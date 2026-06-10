import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, of } from 'rxjs';

interface Sweet {
  id: string;
  name: string;
  category: string;
  price: number;
  quantity: number;
  description?: string;
}

interface PurchaseRequest {
  quantity: number;
}

interface ApiResponse {
  message: string;
  success: boolean;
}

interface UserInfo {
  username: string;
  role: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  sweets: Sweet[] = [];
  filteredSweets: Sweet[] = [];
  isAdmin = false;
  currentUser = '';
  userRole = '';
  isLoading = false;
  showAddForm = false;
  showEditForm = false;
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // Search and filter properties
  searchName = '';
  searchCategory = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;

  // Form properties
  newSweet: Partial<Sweet> = {
    name: '',
    category: '',
    price: 0,
    quantity: 0,
    description: ''
  };
  
  editingSweet: Sweet | null = null;
  purchaseQuantity: { [key: string]: number } = {};

  private apiUrl = 'http://localhost:8080/api';
  private isBrowser: boolean;

  constructor(
    private router: Router,
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    this.checkAuthentication();
    this.loadSweets();
  }

  private checkAuthentication(): void {
    if (!this.isBrowser) return;

    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    const username = localStorage.getItem('username') || sessionStorage.getItem('username');
    const isLoggedIn = localStorage.getItem('isLoggedIn') || sessionStorage.getItem('isLoggedIn');

    if (!token || isLoggedIn !== 'true') {
      this.router.navigate(['/login']);
      return;
    }

    this.currentUser = username || '';
    this.checkUserRole(token);
  }

  private checkUserRole(token: string): void {
    // First, try to get user info to check role
    this.getUserInfo(token);
  }

  private getUserInfo(token: string): void {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    
    // Try to get user information from backend
    this.http.get<UserInfo>(`${this.apiUrl}/user/info`, { headers })
      .pipe(
        catchError(error => {
          console.log('Could not get user info, trying admin check:', error);
          // Fallback: try admin check endpoint
          return this.checkAdminAccess(token);
        })
      )
      .subscribe(userInfo => {
        if (userInfo && typeof userInfo === 'object' && 'role' in userInfo && userInfo.role) {
          this.userRole = userInfo.role;
          this.isAdmin = userInfo.role.toLowerCase() === 'admin';
          
          console.log('User role from backend:', this.userRole);
          console.log('Is admin:', this.isAdmin);
          
          // Store role for future use
          if (this.isBrowser) {
            localStorage.setItem('userRole', this.userRole);
          }
        } else {
          // Fallback to admin check
          this.checkAdminAccess(token).subscribe();
        }
      });
  }

  private checkAdminAccess(token: string) {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    
    return this.http.get(`${this.apiUrl}/admin/check`, { headers, responseType: 'text' })
      .pipe(
        catchError(error => {
          console.log('User is not admin:', error);
          this.isAdmin = false;
          this.userRole = 'USER';
          
          // Check if it's specifically for admin user 'kashyap'
          if (this.currentUser.toLowerCase() === 'kashyap') {
            console.log('Detected admin user kashyap, setting admin privileges');
            this.isAdmin = true;
            this.userRole = 'ADMIN';
          }
          
          return of(null);
        })
      )
      .pipe(
        catchError(() => of(null))
      );
  }

  loadSweets(): void {
    this.isLoading = true;
    
    // Add authorization header for protected endpoints
    const token = this.getAuthToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : undefined;
    
    this.http.get<Sweet[]>(`${this.apiUrl}/sweets`, { headers })
      .pipe(
        catchError(error => {
          console.error('Error loading sweets:', error);
          this.showToastMessage('Error loading sweets', 'error');
          return of([]);
        })
      )
      .subscribe(sweets => {
        this.sweets = sweets;
        this.filteredSweets = [...sweets];
        this.isLoading = false;
        
        console.log('Loaded sweets:', sweets.length);
      });
  }

  searchSweets(): void {
    const params: any = {};
    
    if (this.searchName?.trim()) params.name = this.searchName.trim();
    if (this.searchCategory?.trim()) params.category = this.searchCategory.trim();
    if (this.minPrice !== null && this.minPrice >= 0) params.minPrice = this.minPrice;
    if (this.maxPrice !== null && this.maxPrice >= 0) params.maxPrice = this.maxPrice;

    if (Object.keys(params).length === 0) {
      this.filteredSweets = [...this.sweets];
      return;
    }

    this.isLoading = true;
    const token = this.getAuthToken();
    const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : undefined;
    
    this.http.get<Sweet[]>(`${this.apiUrl}/sweets/search`, { params, headers })
      .pipe(
        catchError(error => {
          console.error('Error searching sweets:', error);
          this.showToastMessage('Error searching sweets', 'error');
          return of([]);
        })
      )
      .subscribe(results => {
        this.filteredSweets = results;
        this.isLoading = false;
      });
  }

  clearSearch(): void {
    this.searchName = '';
    this.searchCategory = '';
    this.minPrice = null;
    this.maxPrice = null;
    this.filteredSweets = [...this.sweets];
  }

  // Admin functions - only available if user is admin
  showAddSweetForm(): void {
    if (!this.isAdminUser()) {
      this.showToastMessage('Admin access required', 'error');
      return;
    }
    
    this.showAddForm = true;
    this.newSweet = { name: '', category: '', price: 0, quantity: 0, description: '' };
  }

  addSweet(): void {
    if (!this.isAdminUser()) {
      this.showToastMessage('Admin access required', 'error');
      return;
    }
    
    if (!this.validateSweetForm(this.newSweet)) return;

    const token = this.getAuthToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.http.post<Sweet>(`${this.apiUrl}/sweets`, this.newSweet, { headers })
      .pipe(
        catchError(error => {
          console.error('Add sweet error:', error);
          this.showToastMessage('Failed to add sweet', 'error');
          return of(null);
        })
      )
      .subscribe(response => {
        if (response) {
          this.showToastMessage('Sweet added successfully!', 'success');
          this.showAddForm = false;
          this.loadSweets();
        }
      });
  }

  // Add these methods to your Dashboard component

getValidQuantity(sweetId: string, maxQuantity: number): number {
  const currentQty = this.purchaseQuantity[sweetId] || 1;
  
  // If current quantity exceeds max, reset to max available
  if (currentQty > maxQuantity) {
    this.purchaseQuantity[sweetId] = Math.min(currentQty, maxQuantity);
    return this.purchaseQuantity[sweetId];
  }
  
  return Math.max(1, Math.min(currentQty, maxQuantity));
}

updatePurchaseQuantity(sweetId: string, event: any, maxQuantity: number): void {
  const inputValue = parseInt(event.target.value) || 1;
  const validValue = Math.max(1, Math.min(inputValue, maxQuantity));
  
  this.purchaseQuantity[sweetId] = validValue;
  
  // Update input field to show corrected value if needed
  if (inputValue !== validValue) {
    event.target.value = validValue;
  }
}

validatePurchaseQuantity(sweetId: string, maxQuantity: number): void {
  const currentQty = this.purchaseQuantity[sweetId] || 1;
  
  if (currentQty > maxQuantity) {
    this.purchaseQuantity[sweetId] = maxQuantity;
    this.showToastMessage(`Quantity adjusted to available stock (${maxQuantity})`, 'error');
  } else if (currentQty < 1) {
    this.purchaseQuantity[sweetId] = 1;
  }
}

isValidPurchaseQuantity(sweetId: string, maxQuantity: number): boolean {
  const quantity = this.purchaseQuantity[sweetId] || 1;
  return quantity >= 1 && quantity <= maxQuantity && maxQuantity > 0;
}

// Update the existing purchaseSweet method with better validation
purchaseSweet(sweet: Sweet): void {
  const quantity = this.getValidQuantity(sweet.id, sweet.quantity);
  
  if (!this.isValidPurchaseQuantity(sweet.id, sweet.quantity)) {
    this.showToastMessage('Please select a valid quantity', 'error');
    return;
  }

  if (!sweet.quantity || sweet.quantity === 0) {
    this.showToastMessage('Item is out of stock', 'error');
    return;
  }

  const request: PurchaseRequest = { quantity };
  const token = this.getAuthToken();
  const headers = token ? new HttpHeaders().set('Authorization', `Bearer ${token}`) : undefined;
  
  this.http.post<ApiResponse>(`${this.apiUrl}/sweets/${sweet.id}/purchase`, request, { headers })
    .pipe(
      catchError(error => {
        console.error('Purchase error:', error);
        this.showToastMessage('Purchase failed', 'error');
        return of({ message: 'Purchase failed', success: false });
      })
    )
    .subscribe(response => {
      if (response.success) {
        this.showToastMessage(`Successfully purchased ${quantity} ${sweet.name}(s)!`, 'success');
        this.purchaseQuantity[sweet.id] = 1; // Reset to 1
        this.loadSweets(); // Refresh the list to get updated stock
      } else {
        this.showToastMessage(response.message || 'Purchase failed', 'error');
        this.loadSweets(); // Refresh to get current stock levels
      }
    });
}

  editSweet(sweet: Sweet): void {
    if (!this.isAdminUser()) {
      this.showToastMessage('Admin access required', 'error');
      return;
    }
    
    this.editingSweet = { ...sweet };
    this.showEditForm = true;
  }

  updateSweet(): void {
    if (!this.isAdminUser()) {
      this.showToastMessage('Admin access required', 'error');
      return;
    }
    
    if (!this.editingSweet || !this.validateSweetForm(this.editingSweet)) return;

    const token = this.getAuthToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.http.put<Sweet>(`${this.apiUrl}/sweets/${this.editingSweet.id}`, this.editingSweet, { headers })
      .pipe(
        catchError(error => {
          console.error('Update sweet error:', error);
          this.showToastMessage('Failed to update sweet', 'error');
          return of(null);
        })
      )
      .subscribe(response => {
        if (response) {
          this.showToastMessage('Sweet updated successfully!', 'success');
          this.showEditForm = false;
          this.editingSweet = null;
          this.loadSweets();
        }
      });
  }

  deleteSweet(sweet: Sweet): void {
    if (!this.isAdminUser()) {
      this.showToastMessage('Admin access required', 'error');
      return;
    }
    
    if (!confirm(`Are you sure you want to delete ${sweet.name}?`)) return;

    const token = this.getAuthToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.http.delete(`${this.apiUrl}/sweets/${sweet.id}`, { headers })
      .pipe(
        catchError(error => {
          console.error('Delete sweet error:', error);
          this.showToastMessage('Failed to delete sweet', 'error');
          return of(null);
        })
      )
      .subscribe(() => {
        this.showToastMessage('Sweet deleted successfully!', 'success');
        this.loadSweets();
      });
  }

  restockSweet(sweet: Sweet): void {
    if (!this.isAdminUser()) {
      this.showToastMessage('Admin access required', 'error');
      return;
    }
    
    const quantity = prompt('Enter restock quantity:');
    if (!quantity || isNaN(+quantity) || +quantity <= 0) {
      this.showToastMessage('Invalid quantity', 'error');
      return;
    }

    const token = this.getAuthToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.http.post<ApiResponse>(`${this.apiUrl}/sweets/${sweet.id}/restock`, 
      { quantity: +quantity }, { headers })
      .pipe(
        catchError(error => {
          console.error('Restock error:', error);
          this.showToastMessage('Failed to restock sweet', 'error');
          return of({ message: 'Restock failed', success: false });
        })
      )
      .subscribe(response => {
        if (response.success) {
          this.showToastMessage(`Successfully restocked ${quantity} ${sweet.name}(s)!`, 'success');
          this.loadSweets();
        } else {
          this.showToastMessage(response.message || 'Restock failed', 'error');
        }
      });
  }

  // Utility methods
  private isAdminUser(): boolean {
    // Check multiple conditions for admin access
    return this.isAdmin || 
           this.userRole === 'ADMIN' || 
           this.currentUser.toLowerCase() === 'kashyap';
  }

  private getAuthToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  }

  private validateSweetForm(sweet: Partial<Sweet>): boolean {
    if (!sweet.name?.trim()) {
      this.showToastMessage('Sweet name is required', 'error');
      return false;
    }
    if (!sweet.category?.trim()) {
      this.showToastMessage('Category is required', 'error');
      return false;
    }
    if (!sweet.price || sweet.price < 0) {
      this.showToastMessage('Valid price is required', 'error');
      return false;
    }
    if (sweet.quantity === undefined || sweet.quantity < 0) {
      this.showToastMessage('Valid quantity is required', 'error');
      return false;
    }
    return true;
  }

  cancelForm(): void {
    this.showAddForm = false;
    this.showEditForm = false;
    this.editingSweet = null;
  }

  logout(): void {
    if (!this.isBrowser) return;
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userRole');
    sessionStorage.removeItem('authToken');
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('isLoggedIn');
    sessionStorage.removeItem('userRole');
    
    this.router.navigate(['/']);
  }

  private showToastMessage(message: string, type: 'success' | 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;

    setTimeout(() => {
      this.showToast = false;
    }, 3000);
  }
}
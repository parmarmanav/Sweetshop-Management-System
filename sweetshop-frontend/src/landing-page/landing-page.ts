import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.css'
})
export class LandingPage {
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  constructor(private router: Router) {}

  // Navigation methods
  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  navigateToRegister(): void {
    this.router.navigate(['/login']); // Navigate to login page, user can switch to register
  }

  navigateToShop(): void {
    // Navigate to shop/products page (create this route if needed)
    this.router.navigate(['/shop']).catch(() => {
      // Fallback: scroll to products section if route doesn't exist
      this.scrollToProducts();
    });
  }

  navigateToAbout(): void {
    // Navigate to dedicated about page (create this route if needed)
    this.router.navigate(['/about']).catch(() => {
      // Fallback: scroll to about section
      this.scrollToSection('about');
    });
  }

  navigateToContact(): void {
    // Navigate to contact page (create this route if needed)
    this.router.navigate(['/contact']).catch(() => {
      // Fallback: scroll to contact section
      this.scrollToSection('contact');
    });
  }

  // Scroll methods for smooth navigation within the page
  scrollToProducts(): void {
    this.scrollToSection('products');
  }

  scrollToSection(sectionId: string): void {
    const element = document.getElementById(sectionId);
    if (element) {
      element.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'start' 
      });
    }
  }

  // Product interaction methods
  viewProduct(productType: string): void {
    // Navigate to individual product page
    this.router.navigate(['/product', productType]).catch(() => {
      // Fallback: show product details in a modal or alert
      this.showToastMessage(`Viewing ${productType} details...`, 'success');
    });
  }

  addToCart(productType: string, event: Event): void {
    // Stop event propagation to prevent card click
    event.stopPropagation();
    
    // Add product to cart logic here
    // For now, just show a success message
    this.showToastMessage(`${productType.charAt(0).toUpperCase() + productType.slice(1)} added to cart!`, 'success');
    
    // Here you would typically:
    // 1. Check if user is logged in
    // 2. Add item to cart service
    // 3. Update cart count in header
    
    // Example of checking login status:
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    if (!token) {
      // User not logged in, redirect to login
      setTimeout(() => {
        this.showToastMessage('Please login to add items to cart', 'error');
        setTimeout(() => {
          this.navigateToLogin();
        }, 1500);
      }, 1000);
    }
  }

  // Toast notification method
  private showToastMessage(message: string, type: 'success' | 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;

    // Auto-hide toast after 3 seconds
    setTimeout(() => {
      this.showToast = false;
    }, 3000);
  }

  // Utility method to check if user is logged in
  isUserLoggedIn(): boolean {
    const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
    const isLoggedIn = localStorage.getItem('isLoggedIn') || sessionStorage.getItem('isLoggedIn');
    return !!(token && isLoggedIn === 'true');
  }

  // Method to handle user authentication state
  handleAuthAction(action: string): void {
    if (this.isUserLoggedIn()) {
      // User is logged in, proceed with action
      switch (action) {
        case 'shop':
          this.navigateToShop();
          break;
        case 'cart':
          this.router.navigate(['/cart']);
          break;
        default:
          break;
      }
    } else {
      // User not logged in, redirect to login
      this.showToastMessage('Please login to continue', 'error');
      setTimeout(() => {
        this.navigateToLogin();
      }, 1500);
    }
  }
}
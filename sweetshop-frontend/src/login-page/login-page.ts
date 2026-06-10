import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { catchError, of } from 'rxjs';

interface LoginData {
  username: string;
  password: string;
  remember: boolean;
}

interface SignupData {
  username: string;
  password: string;
  confirmPassword: string;
  acceptTerms: boolean;
}

interface AuthResponse {
  token?: string;
  message?: string;
  error?: string;
}

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HttpClientModule],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css'
})
export class LoginPage implements OnInit {
  isLoginMode = true;
  showLoginPassword = false;
  showSignupPassword = false;
  showConfirmPassword = false;
  message = '';
  messageType = '';
  isLoading = false;

  private apiUrl = 'http://localhost:8080/api/auth';
  private isBrowser: boolean;

  loginData: LoginData = {
    username: '',
    password: '',
    remember: false
  };

  signupData: SignupData = {
    username: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false
  };

  constructor(
    private router: Router, 
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  switchToSignup(): void {
    this.isLoginMode = false;
    this.clearMessage();
  }

  switchToLogin(): void {
    this.isLoginMode = true;
    this.clearMessage();
  }

  toggleLoginPassword(): void {
    this.showLoginPassword = !this.showLoginPassword;
  }

  toggleSignupPassword(): void {
    this.showSignupPassword = !this.showSignupPassword;
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  passwordsMatch(): boolean {
    return this.signupData.password === this.signupData.confirmPassword;
  }

  onLogin(): void {
    if (!this.loginData.username || !this.loginData.password) {
      this.showMessage('Please fill in all fields', 'error');
      return;
    }

    this.isLoading = true;
    
    const loginRequest = {
      username: this.loginData.username.trim(),
      password: this.loginData.password
    };

    this.http.post(`${this.apiUrl}/login`, loginRequest, { responseType: 'text' })
      .pipe(
        catchError(error => {
          console.error('Login error:', error);
          let errorMessage = 'Login failed. Please try again.';
          
          if (error.status === 401) {
            errorMessage = 'Invalid username or password.';
          } else if (error.status === 400) {
            errorMessage = 'Please enter valid credentials.';
          } else if (error.status === 0) {
            errorMessage = 'Unable to connect to server. Please try again later.';
          } else if (error.error && typeof error.error === 'string') {
            errorMessage = error.error;
          }
          
          this.showMessage(errorMessage, 'error');
          this.isLoading = false;
          return of(null);
        })
      )
      .subscribe(response => {
        this.isLoading = false;
        
        if (response && typeof response === 'string' && response.startsWith('eyJ')) {
          const token = response;
          
          // Only use localStorage/sessionStorage in browser environment
          if (this.isBrowser) {
            if (this.loginData.remember) {
              localStorage.setItem('authToken', token);
              localStorage.setItem('username', this.loginData.username);
              localStorage.setItem('isLoggedIn', 'true');
            } else {
              sessionStorage.setItem('authToken', token);
              sessionStorage.setItem('username', this.loginData.username);
              sessionStorage.setItem('isLoggedIn', 'true');
            }
          }
          
          this.showMessage('Login successful! Redirecting to dashboard...', 'success');
          this.resetLoginForm();
          
          setTimeout(() => {
            this.router.navigate(['/dashboard']).then(
              (success) => {
                if (success) {
                  console.log('Successfully navigated to dashboard');
                } else {
                  console.log('Navigation failed, redirecting to home');
                  this.router.navigate(['/']);
                }
              }
            );
          }, 1500);
        } else {
          // Handle error responses from server
          if (response && typeof response === 'string') {
            this.showMessage(response, 'error');
          } else {
            this.showMessage('Invalid login response from server', 'error');
          }
        }
      });
  }

  onSignup(): void {
    // Frontend validation
    if (!this.signupData.username || !this.signupData.password || !this.signupData.confirmPassword) {
      this.showMessage('Please fill in all fields', 'error');
      return;
    }

    if (!this.passwordsMatch()) {
      this.showMessage('Passwords do not match', 'error');
      return;
    }

    if (this.signupData.password.length < 6) {
      this.showMessage('Password must be at least 6 characters long', 'error');
      return;
    }

    if (!this.signupData.acceptTerms) {
      this.showMessage('Please accept the terms and conditions', 'error');
      return;
    }

    // Trim username
    const trimmedUsername = this.signupData.username.trim();
    if (trimmedUsername.length === 0) {
      this.showMessage('Username cannot be empty', 'error');
      return;
    }

    this.isLoading = true;

    const signupRequest = {
      username: trimmedUsername,
      password: this.signupData.password
    };

    console.log('Sending signup request:', signupRequest);

    this.http.post(`${this.apiUrl}/signup`, signupRequest, { responseType: 'text' })
      .pipe(
        catchError(error => {
          console.error('Signup error:', error);
          let errorMessage = 'Registration failed. Please try again.';
          
          // Handle different error scenarios based on your backend responses
          if (error.status === 409 || error.status === 400) {
            // Backend returns error message as text
            if (error.error && typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.status === 409) {
              errorMessage = 'Username already exists. Please choose a different one.';
            }
          } else if (error.status === 0) {
            errorMessage = 'Unable to connect to server. Please try again later.';
          } else if (error.error && typeof error.error === 'string') {
            errorMessage = error.error;
          }
          
          this.showMessage(errorMessage, 'error');
          this.isLoading = false;
          return of(null);
        })
      )
      .subscribe(response => {
        this.isLoading = false;
        console.log('Signup response:', response);
        
        if (response && typeof response === 'string') {
          // Check if response indicates success
          if (response === 'User registered successfully' || 
              response.toLowerCase().includes('success') || 
              response.toLowerCase().includes('registered')) {
            
            this.showMessage('Account created successfully! Please sign in with your new account.', 'success');
            
            // Switch to login form and reset signup form after short delay
            setTimeout(() => {
              this.switchToLogin();
              this.resetSignupForm();
              
              // Pre-fill the username in login form for better UX
              this.loginData.username = trimmedUsername;
            }, 1500);
            
          } else {
            // Handle error responses from backend
            this.showMessage(response, 'error');
          }
        } else {
          // Fallback for unexpected response format
          this.showMessage('Registration completed! Please sign in with your new account.', 'success');
          setTimeout(() => {
            this.switchToLogin();
            this.resetSignupForm();
            this.loginData.username = trimmedUsername;
          }, 1500);
        }
      });
  }

  private showMessage(message: string, type: 'success' | 'error'): void {
    this.message = message;
    this.messageType = type;
    
    setTimeout(() => {
      this.clearMessage();
    }, 5000);
  }

  private clearMessage(): void {
    this.message = '';
    this.messageType = '';
  }

  private resetLoginForm(): void {
    this.loginData = {
      username: '',
      password: '',
      remember: false
    };
  }

  private resetSignupForm(): void {
    this.signupData = {
      username: '',
      password: '',
      confirmPassword: '',
      acceptTerms: false
    };
  }

  // Check if user is already logged in (only in browser)
  private checkExistingAuth(): void {
    if (!this.isBrowser) {
      return; // Skip localStorage check during SSR
    }

    try {
      const token = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
      const isLoggedIn = localStorage.getItem('isLoggedIn') || sessionStorage.getItem('isLoggedIn');
      
      if (token && isLoggedIn === 'true') {
        this.router.navigate(['/dashboard']);
      }
    } catch (error) {
      console.error('Error checking auth state:', error);
      // Silently fail in case of any storage issues
    }
  }

  ngOnInit(): void {
    // Only check auth state in browser environment
    if (this.isBrowser) {
      this.checkExistingAuth();
    }
  }
}
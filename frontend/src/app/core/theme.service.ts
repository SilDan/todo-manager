import { Injectable } from '@angular/core';
import { SettingsService, ThemeMode } from '../api/settings';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {

  private currentTheme: ThemeMode = 'LIGHT';

  constructor(private readonly settingsService: SettingsService) {}

  initializeTheme(): void {
    this.settingsService.getTheme().subscribe({
      next: (response) => {
        this.currentTheme = response.themeMode;
        this.applyTheme(this.currentTheme);
      },
      error: () => {
        this.applyTheme(this.currentTheme);
      },
    });
  }

  toggleTheme(): void {
    const nextTheme: ThemeMode = this.currentTheme === 'LIGHT' ? 'DARK' : 'LIGHT';

    this.settingsService.updateTheme(nextTheme).subscribe({
      next: (response) => {
        this.currentTheme = response.themeMode;
        this.applyTheme(this.currentTheme);
      },
      error: () => {
        this.applyTheme(this.currentTheme);
      },
    });
  }

  isDarkMode(): boolean {
    return this.currentTheme === 'DARK';
  }

  private applyTheme(themeMode: ThemeMode): void {
    document.body.classList.toggle('dark-mode', themeMode === 'DARK');
  }
}
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type ThemeMode = 'LIGHT' | 'DARK';

export type ThemeSettingsDto = {
  themeMode: ThemeMode;
};

@Injectable({
  providedIn: 'root',
})
export class SettingsService {

  private readonly baseUrl = `${environment.apiUrl}/settings/theme`;

  constructor(private readonly http: HttpClient) {}

  getTheme(): Observable<ThemeSettingsDto> {
    return this.http.get<ThemeSettingsDto>(this.baseUrl);
  }

  updateTheme(themeMode: ThemeMode): Observable<ThemeSettingsDto> {
    return this.http.put<ThemeSettingsDto>(this.baseUrl, { themeMode });
  }
}
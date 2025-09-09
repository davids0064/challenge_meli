import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs/internal/Observable';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardServiceService {

  constructor(private httpClient: HttpClient) { }

  consultarPorCategoria(categoria: any){
    const params = new HttpParams()
    .set('filtro', categoria)
    return this.httpClient.get<any>(`${environment.apiUrl}/api/consultar/consumo-por-categoria`, {params: params});
  }

    consultarPorIp(categoria: any){
    const params = new HttpParams()
    .set('filtro', categoria)
    return this.httpClient.get<any>(`${environment.apiUrl}/api/consultar/consumo-por-ip`, {params: params});
  }

  consultarPorFechas(fechaInicial: any, fechaFinal:any){
    const params = new HttpParams()
    .set('fechaInicial', fechaInicial)
    .set('fechaFinal', fechaFinal)
    return this.httpClient.get<any>(`${environment.apiUrl}/api/consultar/consumo-por-fechas`, {params: params});
  }
}

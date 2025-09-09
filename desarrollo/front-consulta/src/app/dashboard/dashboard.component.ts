import { Component, OnInit } from '@angular/core';
import * as Chartist from 'chartist';
import { DashboardServiceService } from './services/dashboard-service.service';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  cantidadConsultadosHoy:any = 0;
  cantidadConsultadosDosDias:any = 0;
  cantidadConsultadosCincoDias:any = 0;
  verGraficas:any = false;
  formDatosConsulta!: FormGroup;
  datosResultado: any = [];
  fechaActual = new Date();
  fechaInicial = new Date('01/01/1900');
  minFechaIni: Date = new Date();
  maxFechaIni: Date = new Date();
  minFechaFin: Date = new Date();
  maxFechaFin: Date = new Date();

  constructor(
    private dashboardServiceService: DashboardServiceService,
    private formBuilder: FormBuilder
  ) { 
    this.initFormBuilder();
  }

  private initFormBuilder(){
    this.formDatosConsulta = this.formBuilder.group({
      fechaInicio: ['', Validators.required],
      fechaFin: ['', Validators.required],
      filtro: []
    })
  }

  ngOnInit() {
    this.cantidadPeticionesHoy();
    this.cantidadPeticionesUltimosDosDias();
    this.cantidadPeticionesUltimosCincoDias();
  }

  limpiar(){
    
  }

  consultarPorFechas(){
    this.dashboardServiceService.consultarPorFechas(this.formDatosConsulta.get('fechaInicio')?.value, this.formDatosConsulta.get('fechaFin')?.value).subscribe(data => {
      this.verGraficas = true;
      this.datosResultado = data;
      //this.graficaLineal();
      //this.graficaBarras();
      console.log(data)
    });
  }

   chartData$ = new BehaviorSubject<ChartConfiguration<'line'>['data']>({
    labels: [],
    datasets: []
  });

  chartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    plugins: {
      legend: { display: true },
      title: { display: true, text: 'Total de requests por fecha' }
    }
  };

  cantidadPeticionesHoy(){
    const hoy = this.fechaActual.toISOString().split('T')[0]; 
    console.log('fechaActual >>>>>> ' + hoy)
    this.dashboardServiceService.consultarPorFechas(hoy, hoy).subscribe(data => {
      for(let i = 0; data.length >i; i++){
        this.cantidadConsultadosHoy = this.cantidadConsultadosHoy + data[i].count;
      }
    });
  }


    cantidadPeticionesUltimosDosDias(){
    const haceDosDias = new Date(this.fechaActual);
    haceDosDias.setDate(this.fechaActual.getDate() - 2);
    const fechaHaceDosDias  = haceDosDias.toISOString().split('T')[0]; 
    console.log(fechaHaceDosDias)
     const fechaActual = this.fechaActual.toISOString().split('T')[0];
    this.dashboardServiceService.consultarPorFechas(fechaHaceDosDias, fechaActual).subscribe(data => {
      for(let i = 0; data.length >i; i++){
        this.cantidadConsultadosDosDias = this.cantidadConsultadosDosDias + data[i].count;
      }
    });
  }

  cantidadPeticionesUltimosCincoDias(){
    const haceCincoDias = new Date(this.fechaActual);
    haceCincoDias.setDate(this.fechaActual.getDate() - 4);
    const fechaHaceCincoDias  = haceCincoDias.toISOString().split('T')[0]; 
    console.log(fechaHaceCincoDias)
     const fechaActual = this.fechaActual.toISOString().split('T')[0];
    this.dashboardServiceService.consultarPorFechas(fechaHaceCincoDias, fechaActual).subscribe(data => {
      for(let i = 0; data.length >i; i++){
        this.cantidadConsultadosCincoDias = this.cantidadConsultadosCincoDias + data[i].count;
      }
    });
  }

  /*graficaLineal(){

      const dataDailySalesChart: any = {
          labels: ['M', 'T', 'W', 'T', 'F', 'S', 'S'],
          series: [
              [12, 17, 7, 17, 23, 18, 1000]
          ]
      };

     const optionsDailySalesChart: any = {
          lineSmooth: Chartist.Interpolation.cardinal({
              tension: 0
          }),
          low: 0,
          high: 2000, // creative tim: we recommend you to set the high sa the biggest value + something for a better look
          chartPadding: { top: 0, right: 0, bottom: 0, left: 0},
      }

      var dailySalesChart = new Chartist.Line('#dailySalesChart', dataDailySalesChart, optionsDailySalesChart);

      this.startAnimationForLineChart(dailySalesChart);
  }

  startAnimationForLineChart(chart){
      let seq: any, delays: any, durations: any;
      seq = 0;
      delays = 80;
      durations = 500;

      chart.on('draw', function(data) {
        if(data.type === 'line' || data.type === 'area') {
          data.element.animate({
            d: {
              begin: 600,
              dur: 700,
              from: data.path.clone().scale(1, 0).translate(0, data.chartRect.height()).stringify(),
              to: data.path.clone().stringify(),
              easing: Chartist.Svg.Easing.easeOutQuint
            }
          });
        } else if(data.type === 'point') {
              seq++;
              data.element.animate({
                opacity: {
                  begin: seq * delays,
                  dur: durations,
                  from: 0,
                  to: 1,
                  easing: 'ease'
                }
              });
          }
      });

      seq = 0;
  };

  graficaBarras(){
    var datawebsiteViewsChart = {
        labels: ['J', 'F', 'M'],
        series: [
          [542, 443, 320, 780, 553, 453, 326, 434, 568, 610, 756, 895]

        ]
      };
      var optionswebsiteViewsChart = {
          axisX: {
              showGrid: false
          },
          low: 0,
          high: 1000,
          chartPadding: { top: 0, right: 5, bottom: 0, left: 0}
      };
      var responsiveOptions: any[] = [
        ['screen and (max-width: 640px)', {
          seriesBarDistance: 5,
          axisX: {
            labelInterpolationFnc: function (value) {
              return value[0];
            }
          }
        }]
      ];
      var websiteViewsChart = new Chartist.Bar('#websiteViewsChart', datawebsiteViewsChart, optionswebsiteViewsChart, responsiveOptions);

      //start animation for the Emails Subscription Chart
      this.startAnimationForBarChart(websiteViewsChart);
  }

    startAnimationForBarChart(chart){
      let seq2: any, delays2: any, durations2: any;

      seq2 = 0;
      delays2 = 80;
      durations2 = 500;
      chart.on('draw', function(data) {
        if(data.type === 'bar'){
            seq2++;
            data.element.animate({
              opacity: {
                begin: seq2 * delays2,
                dur: durations2,
                from: 0,
                to: 1,
                easing: 'ease'
              }
            });
        }
      });

      seq2 = 0;
  };*/

}

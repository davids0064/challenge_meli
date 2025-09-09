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
  cantidadOk: any = 0;
  cantidadManyToReques: any = 0;
  cantidadUnavailable: any = 0;
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
    this.consultarConsultasPorStatus();
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

  consultarConsultasPorStatus(){
    this.dashboardServiceService.consultarStatus().subscribe(data => {
      for(let i = 0; data.length > i; i++){
        if(data[i].status == 'OK'){
          this.cantidadOk = data[i].count + this.cantidadOk;
        } else if(data[i].status == 'TOO_MANY_REQUESTS'){
          this.cantidadManyToReques = data[i].count + this.cantidadManyToReques;
        } else if(data[i].status == 'SERVICE_UNAVAILABLE'){
          this.cantidadUnavailable = data[i].count + this.cantidadUnavailable;
        }
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

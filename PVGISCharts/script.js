labels = [];
for (i=0;i<24;i++) {
  labels.push(i+"h");
}

const monthNames = ["January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December"
];

var currentDataA;
var currentDataB;
var currentMonth = 1;

var chart;

function displayChart() {
  if (chart) {
    chart.destroy();
  }

  chart = new Chart(document.getElementById("bar-chart-grouped"), {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: "A : "+document.getElementById('a-selector').options[document.getElementById('a-selector').selectedIndex].text,
            backgroundColor: "#3e95cd",
            data: extractDataset(currentDataA)
          }, {
            label: "B : "+document.getElementById('b-selector').options[document.getElementById('b-selector').selectedIndex].text,
            backgroundColor: "#8e5ea2",
            data: extractDataset(currentDataB)
          }
        ]
      },
      options: {
        animation: {
            duration: 200
        },
        title: {
          display: true,
          text: 'Average daily production for '+monthNames[currentMonth-1]+ ' in Wh by hour (UTC) of the day'
        },
        tooltips: {
            callbacks: {
                label: function(tooltipItem, data) {
                    var label = data.datasets[tooltipItem.datasetIndex].label.substr(0, 1) || '';
                    if (label) {
                        label += ': ';
                    }
                    label += Math.round(tooltipItem.yLabel * 100) / 100;
                    label += " Wh"
                    return label;
                }
            }
        },
        scales: {
            yAxes: [{
                ticks: {
                    suggestedMin: 0,
                    suggestedMax: findMax(currentDataA, currentDataB)
                }
            }]
        }
      }
  });
}

function loadMonth(s) {
  currentMonth = s;

  updateDescription();
  displayChart();
}

function loadA(filename) {
  fetch("resources/"+filename+".json")
  .then(function(response) {
    response.json().then(function(data) {
        currentDataA = data;

        updateDescription();
        displayChart();
    });
  })
}

function loadB(filename) {
  fetch("resources/"+filename+".json")
  .then(function(response) {
    response.json().then(function(data) {
        currentDataB = data;

        updateDescription();
        displayChart();
    });
  })
}

function extractDataset(data) {
  if (!data) {
    return [];
  }
  var a = [];
  for (h=0; h<24; h++) {
    a.push(data['data'][currentMonth][h]);
  }
  return a;
}

function updateDescription() {
  if (currentDataA) {
    document.getElementById("a-overall").innerHTML = 
        "Current month overall average daily production: <b>"+computeAverageDaily(currentDataA)+"</b> kWh<br/>"
      + "Yearly overall average production: <b>"+Math.round(currentDataA.overallAverageYearlyProduction/1000)+"</b> kWh<br/>";
  }

  if (currentDataB) {
    document.getElementById("b-overall").innerHTML = 
        "Current month overall average daily production: <b>"+computeAverageDaily(currentDataB)+"</b> kWh<br/>"
      + "Yearly overall average production: <b>"+Math.round(currentDataB.overallAverageYearlyProduction/1000)+"</b> Wh<br/>";
  }
}

function computeAverageDaily(data) {
  var sum = 0;
  for (h=0; h<24; h++) {
    sum += data['data'][currentMonth][h];
  }
  return Number.parseFloat(sum/1000).toFixed(2);
}

function findMax(dataA, dataB) {
  var max = -1;
  for (var m=1; m<=12; m++) {
    for (var h=0; h<24; h++) {
      if (dataA && max < dataA['data'][m][h]) {
        max = dataA['data'][m][h];
      }
      if (dataB && max < dataB['data'][m][h]) {
        max = dataB['data'][m][h];
      }
    }
  }
  return max;
}

document.addEventListener('keydown', keyDownEventListener);
function keyDownEventListener(e) {
    e = e || window.event;
    if (e.keyCode == '38') {
      currentMonth++;
    } else if (e.keyCode == '40') {
      currentMonth--;
    } else {
      return;
    }

    if (currentMonth < 1) {
      currentMonth = 12;
    }
    if (currentMonth > 12) {
      currentMonth = 1;
    }

    document.getElementById("month-selector").value = currentMonth;
    loadMonth(currentMonth);
}

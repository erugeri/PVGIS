# PVGIS

This is a tool to easily compare the efficiency of different solar panels configurations.
This is not self serve but if you understand code a little bit you should be able to quickly adapt it

![example](https://www.rugeri.fr/PVGIS/example.png)

Click [here](https://rugeri.fr/PVGIS/) to test it live.

## How to use

### Get European Commission data
- Go to [PVGIS](https://re.jrc.ec.europa.eu/pvg_tools/fr/#PVP]) 
- Select your location on the map
- Use **Calculated horizon** or upload a custom one
- Select **hourly data** on the left and configure each field of panels
  - enter the **Slope** of your roof
  - enter **Azimuth** (Est is -90° and West 90°)
  - tick **PV Power** and enter your kWp for this panels
- Click on the **csv** download button and save file
- Do it for each of the panels configuration you'd like to compare

### Convert the CSV(s) of all the yearly/daily data to an overall average monthly data in json
- compile and launch the java PVGISDataProcessor project
- call the binary with the following command lines parameters:
	- -i for each input csv filenames you'd like to merge. You can add multiple.
	- -o for the ouput json merged and averaged filename

### Copy json files to PVGISCharts/resources project
- replace the json files and reference them in the `<select id="a-selector"` and `<select id="b-selector"`

### Visualize data
 - open index.html in PVGISCharts
 - select the month you'd like to compare data on
 - select the configuration for A and B
 - you can easily switch months using up and down keyboard arrows

All contributions welcomed! 


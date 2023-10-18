- info.json: 
```

{
"screen": 
	{
		"width":<value>,
		"height":<value>
	},
"sensors":
	[
		{
			"name":"<name>",
			"vendor":"<vendor>"
		}
	]
}
```
- keystroke.csv: [timestamp, orientation, ascii_code, letter] 
- swipe.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, action_type]
- scroll.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, action_type]
- senssor_acc.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_grav.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_gyro.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_lacc.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_magn.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_light.csv: [timestamp, orientation, sensor_data]
- sensor_prox.csv: [timestamp, orientation, sensor_data]
- sensor_temp.csv: [timestamp, orientation, sensor_data]
- sensor_pres.csv: [timestamp, orientation, sensor_data]
- sensor_humd.csv: [timestamp, orientation, sensor_data]

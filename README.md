- info.json: 
```

{
"screen": 
	{
		"width":<value>,
		"height":<value>
	},
"device": 
	{
		"android_version":<value>,
		"device":<value>,
		"model":<value>,
		"brand":<value>,
		"manufacturer":<value>
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
- double_click.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, click_number]
- scroll.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, action_type]
- scale.csv: [timestamp, orientation, x1_coordinate, y1_coordinate, pressure1, x2_coordinate, y2_coordinate, pressure2, action_type]
- clicks.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure]
- video.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, action_type]
- paint.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, action_type]
- long_clicks.csv: [timestamp, orientation, x_coordinate, y_coordinate, pressure, action_type]
- sensor_acc.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_grav.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_gyro.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_lacc.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_magn.csv: [timestamp, orientation, x_axis, y_axis, z_axis]
- sensor_light.csv: [timestamp, orientation, sensor_data]
- sensor_prox.csv: [timestamp, orientation, sensor_data]
- sensor_temp.csv: [timestamp, orientation, sensor_data]
- sensor_pres.csv: [timestamp, orientation, sensor_data]
- sensor_humd.csv: [timestamp, orientation, sensor_data]

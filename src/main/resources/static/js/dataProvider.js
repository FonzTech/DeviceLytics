const dataProvider = {
	xhr: null,
	colors: [
		"255, 99, 132",
        "54, 162, 235",
        "255, 206, 86",
        "75, 192, 192",
        "153, 102, 255",
        "255, 159, 64"
	],
	variables: {},
	
	build: () => {
		dataProvider.variables = [];		
		document.querySelectorAll(".side-menu .mdl-checkbox.is-checked").forEach((e) => {
			const name = e.querySelector("input[type=checkbox]").getAttribute("data-column-name");
			dataProvider.variables.push(name);
		});
	},
	
	isValid: () => {
		return Object.keys(dataProvider.variables).length;
	},
	
	get: (callback, isBoxPlot) => {
		if (dataProvider.xhr !== null)
		{
			dataProvider.xhr.abort();
		}
		
		const program = document.getElementById("programs");
		
		const formData = {
			vars: dataProvider.variables,
			boxPlot: isBoxPlot
		};
		
		dataProvider.xhr = new XMLHttpRequest();
		dataProvider.xhr.open("POST", "/api/get_logs", true);
		dataProvider.xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
		dataProvider.xhr.onreadystatechange = () => {
			if (dataProvider.xhr.readyState === XMLHttpRequest.DONE)
			{
				if (dataProvider.xhr.status == 200)
				{
					// Parse response
					const response = JSON.parse(dataProvider.xhr.responseText);
					
					// Labels and datasets
					const labels = [];
					const datasets = [];

					// Iterate through every log
					Object.keys(dataProvider.variables).forEach((k, i) => {
						// Get correct color
						const color = dataProvider.colors[i % dataProvider.colors.length];
						
						// Create dataset
						const dataset = {
							label: dataProvider.variables[k],
							data: [],
							backgroundColor: "rgba(" + color + ", 0.25)",
							borderColor: "rgba(" + color + ", 1)",
							borderWidth: 1,
							fill: false,
							outlierColor: '#999999',
							padding: 10,
							itemRadius: 0,
						};
						
						// Fill data
						if (isBoxPlot)
						{
							// Push dataset
							dataset.data = response[k];
							
							// Add label
							labels.push(i + 1);
						}
						else
						{
							response.forEach((e) => {
								// Add label
								if (!i)
								{
									labels.push(e.id);
								}
								
								// Dummy data for dataset
								dataset.data.push(parseFloat(e[k]));
							});
						}
							
						// Push dataset
						datasets.push(dataset);
					});
					
					// Launch callback
					callback({
						labels: labels,
						datasets: datasets
					});
				}
				else if (dataProvider.xhr.status != 0)
				{
					alert("Error during the request for the chart data.");
				}
				
				dataProvider.xhr = null;
			}
		};
		dataProvider.xhr.send(JSON.stringify(formData));
	}
};
const actions = {
	DIALOG_WIDE_CLASS: "dialog-wide",
	
	check: () => {
		if (document.querySelector(".side-menu > .mdl-navigation__link") === null)
		{
			const msg = document.createElement("DIV");
			msg.style.marginTop = "16px";
			msg.innerHTML = "No features are currently present. You have to upload a valid dataset first.";
			dialog.show("Alert", msg, dialog.closeAction);
			return true;
		}
		return false;
	},

	history: (isBoxPlot) => {
		// Make dialog normal
		dialog.get().classList.remove(actions.DIALOG_WIDE_CLASS);
		
		// Build data for chart
		dataProvider.build();
		
		// Check if data is available
		if (!dataProvider.isValid())
		{
			const msg = document.createElement("DIV");
			msg.style.marginTop = "16px";
			msg.innerHTML = "You must select at least one variables on the left side!<br>";
			msg.innerHTML += "If no features are present, you have to load a dataset first.";
			dialog.show("Alert", msg, dialog.closeAction);
			return;
		}
		
		// Chart
		{
			const parent = page.getFrameContent();
			
			/*
			{
				const cc = parent.querySelector(".chart-container");
				if (cc !== null)
				{
					cc.parentNode.removeChild(cc);
				}
			}
			*/
			
			const id = "chart_" + Math.round(Math.random() * 100000);
		
			const container = document.createElement("DIV");
			container.setAttribute("class", "side chart-container");
			container.setAttribute("id", id);
			container.style.overflowX = "auto";
			container.style.height = "384px";
			parent.appendChild(container);
			
			const canvas = document.createElement("CANVAS");
			canvas.setAttribute("width", "10000");
			canvas.setAttribute("height", "384");
			canvas.style.height = "100%";
			container.appendChild(canvas);
			
			const close = document.createElement("A");
			close.setAttribute("class", "chart-close");
			close.setAttribute("href", "javascript:void(0)");
			close.onclick = () => {
				parent.removeChild(container);
			};
			close.innerHTML = '<i class="material-icons" style="vertical-align: bottom">close</i> Destroy this chart';
			container.appendChild(close);
			
			dataProvider.get((data) => {
				// Set width
				if (isBoxPlot)
				{
					canvas.setAttribute("width", parseInt(data.labels.length * 256));
				}
				
				// Build chart
				new Chart(canvas, {
					type: isBoxPlot ? "boxplot" : "bar",
					data: data,
					options: {
						responsive: !isBoxPlot,
						maintainAspectRatio: false,
						scales: {
							yAxes: [{
								ticks: {
									beginAtZero: true
								}
							}]
						},
						elements: {
							point: {
								radius: 0
							}
						},
						legend: {
							position: "bottom"
						}
					}
				});
			}, isBoxPlot);
			
			componentHandler.upgradeDom();
		}
	},
	
	train: () => {
		if (actions.check())
		{
			return;
		}

		dialog.callbacks = null;
		actions.showDialog("train_form", "Training");
		
		const select = document.querySelector("dialog select");
		document.querySelectorAll(".side-menu .mdl-navigation__link").forEach((e, i) => {
			const text = e.querySelector("input[type=text]").value;
			select.options.add(new Option(text, i));
		});
	},
		
	predict: () => {
		if (actions.check())
		{
			return;
		}

		dialog.callbacks = null;
		actions.showDialog("predict_form", "Predictions");
	},
	
	showDialog: (id, title) => {
		// Get form to be displayed
		const form = document.getElementById(id).cloneNode(true);
		const file = form.querySelector("input[type=file]");
		const name = form.querySelector(".file_name");
		
		try
		{
			form.querySelector("button").onclick = () => {
				file.click();
			};
			
			file.onchange = () => {
				name.innerHTML = file.files.length ? file.files[0].name : "---";
			};
		}
		catch (err)
		{
		}
		
		// Make dialog wider
		dialog.get().classList.add(actions.DIALOG_WIDE_CLASS);
		
		// Display form in dialog
		dialog.show(title, form, dialog.sendAndCloseAction);
		
		return form;
	}
};
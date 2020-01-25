const actions = {
	DIALOG_WIDE_CLASS: "dialog-wide",
	MODEL_SCORES: {
		"truePositives": "TP",
		"falsePositives": "FP",
		"trueNegatives": "TN",
		"falseNegatives": "FN",
		"precision": "Precision",
		"recall": "Recall"
	},
	
	check: (pred = false) => {
		if (document.querySelector(".side-menu > .mdl-navigation__link") === null)
		{
			const msg = document.createElement("DIV");
			msg.style.marginTop = "16px";
			
			if (pred)
			{
				msg.innerHTML  = "If you want to make predictions, you have to train the model first.";
			}
			else
			{
				msg.innerHTML = "No features are currently present. You have to upload a valid dataset first.";
			}
			
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

		// Set callbacks for dialog
		dialog.callbacks = {
			success: (response) => {
				console.log(response);
				
				// Mark model as trained
				document.querySelector(".side-menu").setAttribute("data-trained", "");
				
				// Show tabs for each class values for target feature
				const tabs = document.getElementById("model_tabs").cloneNode(true);
				tabs.style.marginTop = "24px";
				tabs.removeAttribute("id");
				
				// Add tabs and content
				const divs = [
					tabs.querySelector("div > div:first-child"),
					tabs.querySelector("div > div:last-child")
				];
				
				Object.keys(response.evaluation).forEach((classValue, i) =>
				{
					// Button tab
					const button = document.createElement("BUTTON");
					button.setAttribute("class", "mdl-button mdl-js-button mdl-js-ripple-effect");
					button.setAttribute("data-target", "tab-" + i);
					button.onclick = () => {
						actions.setTabVisible(button);
					};
					button.innerHTML = classValue;
					divs[0].appendChild(button);
					
					// Content tab
					const content = document.createElement("DIV");
					content.setAttribute("class", "tab tab-" + i);
					divs[1].appendChild(content);
					
					// Scores
					Object.keys(actions.MODEL_SCORES).forEach((key, j) =>
					{
						const text = document.createElement("DIV");
						text.innerHTML = actions.MODEL_SCORES[key] + " = ";
						text.innerHTML += j >= 4 ? (parseFloat(response.evaluation[classValue][key]) * 100.0).toFixed(2) + "%" : response.evaluation[classValue][key];
						content.appendChild(text);
					});
				});
					
				// Trigger first tab
				tabs.querySelector("div > div:first-child > button:first-child").onclick();
				
				dialog.show(null, tabs, null);
			},
			failure: () => {
				// Mark model as to be trained
				document.querySelector(".side-menu").removeAttribute("data-trained");
			}
		};
		actions.showDialog("train_form", "Training");
		
		// Populate select list with dataset features
		const select = document.querySelector("dialog select");
		document.querySelectorAll(".side-menu .mdl-navigation__link").forEach((e, i) => {
			const text = e.querySelector("input[type=text]").value;
			select.options.add(new Option(text, i));
		});
	},
		
	predict: () => {
		if (actions.check(true))
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
	},
	
	setTabVisible: (button) => {
		const clazz = button.getAttribute("data-target");
		
		// Set button visible
		button.parentNode.parentNode.querySelectorAll("div > div:first-child > button").forEach((e) => {
			if (e == button)
			{
				e.classList.add("selected");
			}
			else
			{
				e.classList.remove("selected");
			}
		});
		
		// Set tab visible
		button.parentNode.parentNode.querySelectorAll("div > div:last-child > .tab").forEach((e) => {
			e.style.display = e.classList.contains(clazz) ? "flex" : "none";
		});
	}
};
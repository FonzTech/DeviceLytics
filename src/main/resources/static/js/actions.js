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
		if ((pred && !document.querySelector(".side-menu").hasAttribute("data-trained")) || document.querySelector(".side-menu > .mdl-navigation__link") === null)
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
		
			const outer = document.createElement("DIV");
			outer.setAttribute("class", "side chart-container");
			outer.setAttribute("id", id);
			parent.appendChild(outer);
		
			const container = document.createElement("DIV");
			container.style.overflowX = "auto";
			container.style.height = "384px";
			outer.appendChild(container);
			
			const canvas = document.createElement("CANVAS");
			canvas.setAttribute("data-type", "bar");
			canvas.setAttribute("width", "10000");
			canvas.setAttribute("height", "384");
			canvas.style.height = "100%";
			container.appendChild(canvas);
			
			const variables = dataProvider.variables.slice();
			
			const fx = () => {
				dataProvider.get((data) => {
					// Set width
					if (isBoxPlot)
					{
						canvas.setAttribute("width", parseInt(data.datasets.length * 256));
					}
					
					// Build chart
					new Chart(canvas, {
						type: isBoxPlot ? "boxplot" : canvas.getAttribute("data-type"),
						data: data,
						options: {
							responsive: !isBoxPlot,
							maintainAspectRatio: false,
							scales: {
								yAxes: [{
									ticks: {
										beginAtZero: false
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
				}, isBoxPlot, variables);
			};
			
			{
				const right = document.createElement("DIV");
				right.setAttribute("class", "chart-close");
				outer.appendChild(right);
				
				if (!isBoxPlot)
				{
					const close = document.createElement("A");
					close.setAttribute("href", "javascript:void(0)");
					close.onclick = () => {
						canvas.setAttribute("data-type", canvas.getAttribute("data-type") == "bar" ? "line" : "bar");
						fx();
					};
					close.style.marginRight = "32px";
					close.innerHTML = '<i class="material-icons" style="vertical-align: bottom">insert_chart_outlined</i> Switch between line and bar chart';
					right.appendChild(close);
				}
				
				{
					const close = document.createElement("A");
					close.setAttribute("href", "javascript:void(0)");
					close.onclick = () => {
						parent.removeChild(outer);
					};
					close.innerHTML = '<i class="material-icons" style="vertical-align: bottom">close</i> Destroy this chart';
					right.appendChild(close);
				}
			}
			
			fx();
			
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
				// Mark model as trained
				document.querySelector(".side-menu").setAttribute("data-trained", response.classIndex);
				
				// Highlight training features in side menu
				const vars = document.querySelectorAll(".side-menu .mdl-navigation__link > input[type=text]");
				response.trainVars.forEach((e) => {
					vars[e].classList.add("trained");
				});
				
				// Show tabs for each class values for target feature
				const tabs = document.getElementById("model_tabs").cloneNode(true);
				tabs.style.marginTop = "24px";
				tabs.removeAttribute("id");
				
				// Add tabs and content
				const divs = [
					tabs.querySelector("div > div:nth-child(1)"),
					tabs.querySelector("div > div:nth-child(2)"),
					tabs.querySelector("div > div:nth-child(3)")
				];
				
				// Display message
				divs[0].innerHTML = response.message;
				
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
					divs[1].appendChild(button);
					
					// Content tab
					const content = document.createElement("DIV");
					content.setAttribute("class", "tab tab-" + i);
					divs[2].appendChild(content);
					
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
				tabs.querySelector('button[data-target="tab-0"]').click();
				
				dialog.show(null, tabs, null);
			},
			failure: () => {
				// Mark model as to be trained
				document.querySelector(".side-menu").removeAttribute("data-trained");
			}
		};
		actions.showDialog("train_form", "Training");
		
		// Populate select list with dataset features
		const vars = document.querySelector("dialog .vars-list > .spacer");
		const select = document.querySelector("dialog select");
		
		// Clear containers
		while (vars.firstChild !== null)
		{
			vars.removeChild(vars.firstChild);
		}
		
		while (select.options.length > 1)
		{
			select.remove(1);
		}
		
		// Append variables
		document.querySelectorAll(".side-menu .mdl-navigation__link").forEach((e, i) => {
			const input = e.querySelector("input[type=text]");
			text = input.value.replace(/\s/g, "").length ? input.value : input.getAttribute("placeholder")
			select.options.add(new Option(text, i));
			
			const id = "id_" + Math.round(Math.random() * 10000);
			
			const label = document.createElement("LABEL");
			label.setAttribute("class", "mdl-checkbox mdl-js-checkbox mdl-js-ripple-effect");
			label.setAttribute("for", id);
			vars.appendChild(label);
			
			const checkbox = document.createElement("INPUT");
			checkbox.setAttribute("type", "checkbox");
			checkbox.setAttribute("id", id);
			checkbox.setAttribute("class", "mdl-checkbox__input");
			checkbox.setAttribute("checked", "checked");
			checkbox.setAttribute("name", "vars");
			checkbox.setAttribute("value", i);
			label.appendChild(checkbox);
			
			const span = document.createElement("SPAN");
			span.setAttribute("class", "mdl-checkbox__label");
			span.innerHTML = text;
			label.appendChild(span);
					
			vars.appendChild(document.createTextNode(" "));
		});
		
		vars.appendChild(document.createElement("SPAN"));
		
		componentHandler.upgradeDom();
	},
		
	predict: () => {
		if (actions.check(true))
		{
			return;
		}

		dialog.callbacks = null;
		actions.showDialog("predict_form", "Predictions");
		
		const index = document.querySelector(".side-menu").getAttribute("data-trained");
		dialog.get().querySelector("input[type=hidden]").value = index;
		
		const input = document.querySelectorAll(".side-menu .mdl-navigation__link > input[type=text]")[index];
		text = input.value.replace(/\s/g, "").length ? input.value : input.getAttribute("placeholder");
		dialog.get().querySelector(".target-var").innerHTML = text;
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
		button.parentNode.parentNode.querySelectorAll("div > div:nth-child(2) > button").forEach((e) => {
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
		button.parentNode.parentNode.querySelectorAll("div > div:nth-child(3) > .tab").forEach((e) => {
			e.style.display = e.classList.contains(clazz) ? "flex" : "none";
		});
	}
};
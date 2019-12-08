const utility = {
	cleanMdlElement: (element) => {
		if (element !== null && element.nodeType == Node.ELEMENT_NODE)
		{
			element.removeAttribute("data-upgraded");
			element.querySelectorAll("*[data-upgraded]").forEach((e) => {
				e.removeAttribute("data-upgraded");
			});
		}
		return element;
	}
};

const page = {
	categories: [],
	items: [],
	cards: [
		{
			bg: "bg-forecast.jpg",
			title: "Predizioni",
			description: "Applica le tecniche di predizione dei guasti."
		},
		{
			bg: "bg-time.jpg",
			title: "Storico",
			description: "Visualizza lo storico dei dati per questa tipologia."
		},
		{
			bg: "bg-train.jpg",
			title: "Addestra Modello",
			description: "Aggiungi dati reali per arricchire il modello."
		}
	],
	drawerToggle: null,
	frameContent: null,
	
	getDrawerToggle: () => {
		if (page.drawerToggle === null)
		{
			page.drawerToggle = document.querySelector(".mdl-layout.has-drawer > .mdl-layout__drawer-button");
		}
		return page.drawerToggle;
	},
	
	getFrameContent: () => {
		if (page.frameContent === null)
		{
			page.frameContent = document.getElementById("frame-content");
		}
		return page.frameContent;
	},
	
	scroll: (o) => {
		const toggle = page.getDrawerToggle();
		toggle.style.color = o.scrollTop > toggle.offsetHeight / 2 ? "black" : "white";
	},
	
	fillPrograms: () => {
		// Get programs list
		const programs = document.getElementById("programs");
		
		// Remove variables from programs list
		while (programs.options.length)
		{
			programs.remove(0);
		}
		
		// Repopulate programs
		if (page.categories.length)
		{
			page.categories.forEach((e, index) => {
				const opt = new Option(e.program + " - " + e.position, index);
				opt.setAttribute("data-program", e.program);
				opt.setAttribute("data-position", e.position);
				programs.options.add(opt);
			});
		}
		else
		{
			programs.options.add(new Option("---", -1));
		}
	},
	
	fillVariables: () => {		
		// Get side menu
		const parent = document.querySelector(".side-menu");
		
		// Remove variables from side menu
		while (parent.firstChild !== null)
		{
			parent.removeChild(parent.firstChild);
		}
		
		// Repopulate variables
		page.items.forEach((e, index) => {
			// Create anchor
			const div = document.createElement("DIV");
			div.setAttribute("class", "mdl-navigation__link");
			div.setAttribute("href", "javascript:void(0)"); // Avoid following hyperlink
			parent.appendChild(div);
			
			// Selectable checkbox with label
			{
				const id = "cb" + Math.round(Math.random() * 100000);
				
				const label = document.createElement("LABEL");
				label.setAttribute("class", "mdl-checkbox mdl-js-checkbox mdl-js-ripple-effect");
				label.setAttribute("for", id);
				div.appendChild(label);
				
				const input = document.createElement("INPUT");
				input.setAttribute("class", "mdl-checkbox__input");
				input.setAttribute("type", "checkbox");
				input.setAttribute("id", id);
				input.setAttribute("data-column-name", e.columnName);
				label.appendChild(input);
				
				const span = document.createElement("SPAN");
				span.setAttribute("class", "mdl-checkbox__label");
				span.innerHTML = e.columnLabel;
				label.appendChild(span);

			}
		});
		
		// Update the DOM
		componentHandler.upgradeDom();
	},
	
	load: (index) => {
		// Clear content
		const parent = page.getFrameContent();
		while (parent.firstChild !== null)
		{
			parent.removeChild(parent.firstChild);
		}
		
		// Display cards
		{
			const container = document.createElement("DIV");
			container.setAttribute("class", "spacer side");
			parent.appendChild(container);
			
			for (let i = 0; i < page.cards.length; ++i)
			{
				if (i)
				{
					container.appendChild(document.createTextNode(" "));
				}
			
				const card = components.getCard(page.cards[i].title, page.cards[i].description);
				card.classList.add("chart-card");
				card.onclick = () => {
					page.clickCard(i);
				};
				container.appendChild(card);
				
				const bg = card.querySelector(".mdl-card__title");
				bg.style.backgroundImage = "url(res/" + page.cards[i].bg + ")";
			}
			
			const span = document.createElement("SPAN");
			container.appendChild(span);
		}
		
		// Separator
		{
			parent.appendChild(document.createElement("HR"));
		}
		
		// Check if variables should be loaded
		if (index == -1)
		{
			const formData = new FormData();
			formData.append("s", "vars");
			
			const xhr = new XMLHttpRequest();
			xhr.open("POST", "Api", true);
			xhr.onreadystatechange = () => {
				if (xhr.readyState === XMLHttpRequest.DONE)
				{
					if (xhr.status == 200)
					{
						const response = JSON.parse(xhr.responseText);
						page.categories = response.categories;
						page.items = response.variables;
						page.fillPrograms();
						page.fillVariables();
					}
					else if (xhr.status != 0)
					{
						alert("Errore durante la ricezione delle variabili: ricevuto Codice HTTP " + xhr.status);
					}
				}
			};
			xhr.send(formData);
		}
	},
	
	clickCard: (actionId) => {
		switch (actionId)
		{
		case 0:
			actions.predict();
			break;

		case 1:
			actions.history();
			break;

		case 2:
			actions.train();
			break;
		}
	}
};

const actions = {
	DIALOG_WIDE_CLASS: "dialog-wide",
		
	predict: () => {
		// Get form to be displayed
		const form = document.getElementById("predict_form").cloneNode(true);
		
		// Make dialog wider
		dialog.get().classList.add(actions.DIALOG_WIDE_CLASS);
		
		// Display form in dialog
		dialog.show("Predizioni", form, dialog.closeAction);
	},
		
	history: () => {
		// Make dialog normal
		dialog.get().classList.remove(actions.DIALOG_WIDE_CLASS);
		
		// Build data for chart
		dataProvider.build();
		
		// Check if data is available
		if (!dataProvider.isValid())
		{
			const text = document.createTextNode("Devi selezionare almeno un programma / posizione e una variabile!");
			dialog.show("Avviso", text, dialog.closeAction);
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
			close.innerHTML = '<i class="material-icons" style="vertical-align: bottom">close</i> Chiudi Questo Grafico';
			container.appendChild(close);
			
			dataProvider.get((data) => {
				new Chart(canvas, {
					type: "line",
					data: data,
					options: {
						responsive: true,
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
						}
					}
				});
			});
			
			componentHandler.upgradeDom();
		}
	},
	
	train: () => {
		// Get form to be displayed
		const form = document.getElementById("train_form").cloneNode(true);
		const file = form.querySelector("input[type=file]");
		const name = form.querySelector(".file_name");
		
		form.querySelector("button").onclick = () => {
			file.click();
		};
		
		file.onchange = () => {
			name.innerHTML = file.files.length ? file.files[0].name : "---";
		};
		
		// Make dialog wider
		dialog.get().classList.add(actions.DIALOG_WIDE_CLASS);
		
		// Display form in dialog
		dialog.show("Addestramento", form, dialog.sendAndCloseAction);
	}
};

const dialog = {
	element: null,
	xhr: null,
	closeAction: {
		"Chiudi": () => {
			dialog.hide();
		}
	},
	sendAndCloseAction: {
		"Invia": () => {
			console.log("ok");
			const formData = new FormData(dialog.get().querySelector("form"));
			
			dialog.xhr = new XMLHttpRequest();
			dialog.xhr.open("POST", "Api", true);
			dialog.xhr.onreadystatechange = () => {
				if (dialog.xhr.readyState === XMLHttpRequest.DONE)
				{
					if (dialog.xhr.status == 200)
					{
						// Parse response
						const response = JSON.parse(dialog.xhr.responseText);
						
						console.log(response);
					}
					else if (dialog.xhr.status != 0)
					{
						alert("Errore durante l'invio dei dati.");
					}
					
					dialog.xhr = null;
					dialog.hide();
				}
			};
			dialog.xhr.send(formData);
			
			{
				const loading = document.getElementById("loading_form").cloneNode(true);
				dialog.show(null, loading, dialog.closeAction);
			}
		},
		"Chiudi": () => {
			dialog.hide();
		}
	},
	
	get: () => {
		if (dialog.element === null)
		{
			dialog.element = document.querySelector("dialog");
		}
		return dialog.element;
	},
	
	show: (title, contentDom, actions) => {
		const d = dialog.get();
		
		// Set title
		if (title !== null)
		{
			d.querySelector(".mdl-dialog__title").innerHTML = title;
		}
		
		// Set content
		if (contentDom !== null)
		{
			const content = d.querySelector(".mdl-dialog__content");
			while (content.firstChild !== null)
			{
				content.removeChild(content.firstChild);
			}
			content.appendChild(utility.cleanMdlElement(contentDom));
			componentHandler.upgradeDom();
		}
		
		// Setup actions
		if (actions !== null)
		{
			const buttons = d.querySelector(".mdl-dialog__actions");
			
			while (buttons.firstChild !== null)
			{
				buttons.removeChild(buttons.firstChild);
			}
			
			Object.keys(actions).forEach((k, i) => {
				const b = document.createElement("BUTTON");
				b.setAttribute("class", "mdl-button");
				b.setAttribute("type", "button");
				b.onclick = actions[k];
				b.innerHTML = k;
				buttons.appendChild(b);
			});
		}
		
		if (!d.hasAttribute("open"))
		{
			d.showModal();
		}
	},
	
	hide: () => {
		dialog.get().close();
	}
};

const components = {
	getCard: (title, description) => {
		const card = document.getElementById("dummy-card").cloneNode(true);
		card.querySelector(".mdl-card__title-text").innerHTML = title;
		card.querySelector(".mdl-card__supporting-text").innerHTML = description;
		card.removeAttribute("id");
		card.removeAttribute("id");
		return card;
	}
};

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
		dataProvider.variables = {};		
		document.querySelectorAll(".side-menu .mdl-checkbox.is-checked").forEach((e) => {
			const key = e.getAttribute("for");
			const label = e.querySelector(".mdl-checkbox__label").innerHTML;
			const name = e.querySelector("input[type=checkbox]").getAttribute("data-column-name");
			dataProvider.variables[name] = label;
		});
	},
	
	isValid: () => {
		return document.getElementById("programs").value != "-1" && Object.keys(dataProvider.variables).length;
	},
	
	get: (callback) => {		
		if (dataProvider.xhr !== null)
		{
			dataProvider.xhr.abort();
		}
		
		const program = document.getElementById("programs");
		
		const formData = new FormData();
		formData.append("s", "values");
		formData.append("program", program.options[program.selectedIndex].getAttribute("data-program"));
		formData.append("position", program.options[program.selectedIndex].getAttribute("data-position"));
		formData.append("vars", Object.keys(dataProvider.variables).join(","));
		
		dataProvider.xhr = new XMLHttpRequest();
		dataProvider.xhr.open("POST", "Api", true);
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
							fill: false
						};
						
						// Fill data
						response.forEach((e) => {
							// Fill labels
							if (!i)
							{
								labels.push(e.id);
							}
							
							// Dummy data for dataset
							dataset.data.push(parseFloat(e[k]));
						});
							
						// Push dataset
						datasets.push(dataset);
					});
					
					callback({
						labels: labels,
						datasets: datasets
					});
				}
				else if (dataProvider.xhr.status != 0)
				{
					alert("Errore durante l'ottenimento dei dati per il grafico.");
				}
				
				dataProvider.xhr = null;
			}
		};
		dataProvider.xhr.send(formData);
	}
};
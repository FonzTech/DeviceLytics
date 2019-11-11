const page = {
	categories: [],
	items: [],
	cards: [
		{
			bg: "bg-forecast.jpg",
			title: "Previsioni",
			description: "Applica le tecniche di previsione dei guasti."
		},
		{
			bg: "bg-time.jpg",
			title: "Storico",
			description: "Visualizza lo storico dei dati per questa tipologia."
		},
		{
			bg: "bg-last.jpg",
			title: "Ultimo Periodo",
			description: "Visualizza i dati raccolti nell'ultimo periodo."
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
					page.clickCard(card);
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
					else
					{
						alert("Errore durante la ricezione delle variabili: ricevuto Codice HTTP " + xhr.status);
					}
				}
			};
			xhr.send(formData);
		}
	},
	
	clickCard: (o) => {
		// Build data for chart
		dataProvider.build();
		
		// Check if data is available
		if (!dataProvider.isValid())
		{
			dialog.show("Avviso", "Devi selezionare almeno un programma / posizione e una variabile!", dialog.acceptAction);
			return;
		}
		
		// Chart
		{
			const parent = page.getFrameContent();
			
			{
				const cc = parent.querySelector(".chart-container");
				if (cc !== null)
				{
					cc.parentNode.removeChild(cc);
				}
			}
		
			const container = document.createElement("DIV");
			container.setAttribute("class", "side chart-container");
			container.style.overflowX = "auto";
			container.style.height = "384px";
			parent.appendChild(container);
			
			const canvas = document.createElement("CANVAS");
			canvas.setAttribute("width", "10000");
			canvas.setAttribute("height", "384");
			canvas.style.height = "100%";
			container.appendChild(canvas);
			
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
			})
		}
	}
};

const dialog = {
	element: null,
	acceptAction: {
		"OK": () => {
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
	
	show: (title, content, actions) => {
		const d = dialog.get();
		
		d.querySelector(".mdl-dialog__title").innerHTML = title;
		d.querySelector(".mdl-dialog__content").innerHTML = content;
		
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
		
		d.showModal();
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
				else
				{
					alert("Errore durante l'ottenimento dei dati per il grafico.");
				}
				
				dataProvider.xhr = null;
			}
		};
		dataProvider.xhr.send(formData);
	}
};
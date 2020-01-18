const page = {
	xhr: null,
	items: [],
	cards: [
		{
			bg: "img/bg-time.jpg",
			title: "History",
			description: "View data history for selected variables."
		},
		{
			bg: "img/bg-time.jpg",
			title: "Box Plot",
			description: "View box plot for selected variables."
		},
		{
			bg: "img/bg-train.jpg",
			title: "Train Model",
			description: "Add new features to the dataset for the model."
		},
		{
			bg: "img/bg-forecast.jpg",
			title: "Predictions",
			description: "Apply prediction techniques to data"
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
	
	fillVariables: () => {		
		// Get side menu
		const parent = document.querySelector(".side-menu");
		
		// Remove variables from side menu
		while (parent.firstChild !== null)
		{
			parent.removeChild(parent.firstChild);
		}
		
		// Repopulate variables
		page.items.forEach((e) => {
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
				
				const checkbox = document.createElement("INPUT");
				checkbox.setAttribute("class", "mdl-checkbox__input");
				checkbox.setAttribute("type", "checkbox");
				checkbox.setAttribute("id", id);
				checkbox.setAttribute("data-column-name", e);
				label.appendChild(checkbox);
				
				const span = document.createElement("LABEL");
				span.setAttribute("class", "mdl-checkbox__label");
				label.appendChild(span);
				
				const input = document.createElement("INPUT");
				input.setAttribute("type", "text");
				input.setAttribute("placeholder", e);
				input.value = e;
				div.appendChild(input);

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
				bg.style.backgroundImage = "url(" + page.cards[i].bg + ")";
			}
			
			const span = document.createElement("SPAN");
			container.appendChild(span);
		}
		
		// Separator
		{
			parent.appendChild(document.createElement("HR"));
		}
	},
	
	clickCard: (actionId) => {
		switch (actionId)
		{
		case 0:
			actions.history(false);
			break;
			
		case 1:
			actions.history(true);
			break;

		case 2:
			actions.train();
			break;

		case 3:
			actions.predict();
			break;
		}
	},
};
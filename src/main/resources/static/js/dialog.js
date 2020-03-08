const dialog = {
	element: null,
	xhr: null,
	callbacks: null,
	closeAction: {
		"Close": () => {
			dialog.hide();
		}
	},
	sendAndCloseAction: {
		"Send": () => {
			const form = dialog.get().querySelector("form");
			
			const formData = new FormData(form);
			
			dialog.xhr = new XMLHttpRequest();
			dialog.xhr.open(form.method, form.action, true);
			dialog.xhr.onreadystatechange = () => {
				if (dialog.xhr.readyState === XMLHttpRequest.DONE)
				{
					if (dialog.xhr.status == 200)
					{
						// Parse response
						const response = JSON.parse(dialog.xhr.responseText);
						
						// Show error if required
						const message = document.getElementById("message_form").cloneNode(true);
						const comp = [
							message.querySelector("h5"),
							message.querySelector("div")
						];

						comp[0].innerHTML = response.success ? "Congratulations" : "Error encountered";
						comp[1].innerHTML = response.message;
						
						const isTableShown = response.success && Array.isArray(response.features) && response.features.length;
						
						if (isTableShown)
						{
							// Get actual column names
							let columns = {};
							document.querySelectorAll(".side-menu .mdl-navigation__link input[type=text]").forEach((e) => {
								const key = e.getAttribute("placeholder");
								columns[key] = e.value.length ? e.value : key;
							});
							const columnKeys = Object.keys(columns);
							
							// Container
							const div = document.createElement("DIV");
							div.setAttribute("class", "mdl-shadow--2dp predicted");
							message.appendChild(div);
							
							// Table
							for (let i = 0; i < 2; ++i)
							{
								const table = document.createElement("TABLE");
								table.setAttribute("class", "mdl-data-table mdl-js-data-table");
								
								// Wrap table if necessary
								if (i == 1)
								{
									// Container
									const innerDiv = document.createElement("DIV");
									innerDiv.setAttribute("class", "table-container");
									innerDiv.appendChild(table);
									div.appendChild(innerDiv);
								}
								else
								{
									div.appendChild(table);
								}
	
								// Head of table
								{
									const thead = document.createElement("THEAD");
									table.appendChild(thead);
	
									const tr = document.createElement("TR");
									thead.appendChild(tr);
									
									if (i == 0)
									{
										const th = document.createElement("TH");
										th.innerHTML = "Predicted";
										tr.appendChild(th);
									}
									else
									{
										columnKeys.forEach((key) => {
											const th = document.createElement("TH");
											th.innerHTML = columns[key];
											tr.appendChild(th);
										});
									}
								}
	
								// Body of table
								{
									const tbody = document.createElement("TBODY");
									table.appendChild(tbody);
									
									response.features.forEach((e) => {
										const tr = document.createElement("TR");
										tbody.appendChild(tr);
										
										if (i == 0)
										{
											const td = document.createElement("TD");
											td.innerHTML = e.label;
											tr.appendChild(td);
										}
										else
										{
											columnKeys.forEach((key) => {
												const td = document.createElement("TD");
												td.innerHTML = e.instance[key];
												tr.appendChild(td);
											});
										}
									});
								}
							}
						}

						// Update dialog
						dialog.show(null, message, dialog.closeAction);

						if (isTableShown)
						{
							dialog.get().classList.remove("dialog-wide");
							dialog.get().classList.add("dialog-ultrawide");
						}
						
						// Launch callbacks
						if (dialog.callbacks !== null)
						{
							if (response.success)
							{
								if (dialog.callbacks.success !== null)
								{
									dialog.callbacks.success(response);
								}
							}
							else if (dialog.callbacks.failure !== null)
							{
								dialog.callbacks.failure();
							}
						}
					}
					else if (dialog.xhr.status != 0)
					{
						const message = document.getElementById("message_form").cloneNode(true);
						message.querySelector("h5").innerHTML = "Fatal error";
						message.querySelector("div").innerHTML = "Response code was " + dialog.xhr.status + ".";
						
						dialog.show(null, message, dialog.closeAction);
					}
					
					dialog.xhr = null;
				}
			};
			dialog.xhr.send(formData);
			
			// Show loading
			{
				const loading = document.getElementById("loading_form").cloneNode(true);
				dialog.show(null, loading, dialog.closeAction);
			}
		},
		"Close": () => {
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

		d.classList.add("dialog-wide");
		d.classList.remove("dialog-ultrawide");
	},
	
	hide: () => {
		dialog.get().close();
	}
};
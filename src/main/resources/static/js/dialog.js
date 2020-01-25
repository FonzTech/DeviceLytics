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
						
						const isTableShown = "columns" in response && response.columns !== null;
						
						if (isTableShown)
						{							
							const keys = Object.keys(response.columns);
							keys.unshift(null);
							
							// Container
							const div = document.createElement("DIV");
							div.setAttribute("class", "table-container");
							message.appendChild(div);
							
							// Table
							const table = document.createElement("TABLE");
							table.setAttribute("class", "mdl-data-table mdl-js-data-table mdl-shadow--2dp");
							div.appendChild(table);

							// Head of table
							{
								const thead = document.createElement("THEAD");
								table.appendChild(thead);

								const tr = document.createElement("TR");
								thead.appendChild(tr);
								
								keys.forEach((e) => {
									const th = document.createElement("TH");
									th.setAttribute("data-key", e);
									th.innerHTML = e === null ? "Label" : response.columns[e];
									tr.appendChild(th);
								});
							}

							// Body of table
							{
								const tbody = document.createElement("TBODY");
								table.appendChild(tbody);
								
								response.features.forEach((e) => {
									const tr = document.createElement("TR");
									tbody.appendChild(tr);
									
									keys.forEach((k) => {
										const td = document.createElement("TD");
										td.innerHTML = k === null ? e.label : e.instance[k];
										tr.appendChild(td);
									});
								});
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
						alert("Errore durante l'invio dei dati.");
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
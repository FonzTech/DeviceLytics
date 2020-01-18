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

const session = {
	xhr: null,
	
	start: () => {
		const button = document.querySelectorAll(".session-div > button");
		button[0].style.display = "none";
		button[1].style.display = "initial";
	},

	load: () => {
		dialog.callbacks = {
			success: () => {
				// Get variables
				session.getVars();
				
				// Enable buttons on sidebar
				session.start();
			},
			failure: null
		};
		session.form = actions.fileUpload("load_form", "Load Dataset");
	},
	
	getVars: () => {
		if (session.xhr !== null)
		{
			session.xhr.abort();
		}
		
		session.xhr = new XMLHttpRequest();
		session.xhr.open("POST", "/api/get_vars", true);
		session.xhr.onreadystatechange = () => {
			if (session.xhr.readyState === XMLHttpRequest.DONE)
			{
				if (session.xhr.status == 200)
				{
					const response = JSON.parse(session.xhr.responseText);
					if (response.success)
					{
						page.items = response.variables;
						page.fillVariables();
					}
					else
					{
						dialog.show(null, response.message !== null ? response.message : "Unknown error encourtered.", dialog.closeAction);
					}
				}
				else if (session.xhr.status != 0)
				{
					alert("Error while getting variables. Received HTTP code: " + session.xhr.status);
				}
			}
		};
		session.xhr.send();
	}
};
document.addEventListener("DOMContentLoaded", () => {
    const localCreatedAtInput = document.getElementById("localCreatedAt");
    const now = new Date();
    localCreatedAtInput.value = now.toISOString().slice(0, 16); // yyyy-MM-ddTHH:mm

    const projectSelect = document.getElementById("projectSelect");
    const catalogSelect = document.getElementById("catalogSelect");
    let allCatalogs = [];

    fetch("/api/v3/projects")
        .then(response => response.json())
        .then(projects => {
            projects.forEach(project => {
                const option = document.createElement("option");
                option.value = project.slug;
                option.textContent = project.name;
                projectSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error("Error fetching projects:", error);
            alert("Failed to load projects");
        });

    fetch("/api/v3/projects/catalogs")
        .then(response => response.json())
        .then(catalogs => {
            allCatalogs = catalogs;
            populateCatalogs("");
        })
        .catch(error => {
            console.error("Error fetching catalogs:", error);
            alert("Failed to load catalogs");
        });

    function populateCatalogs(projectSlug) {
        catalogSelect.innerHTML = '<option value="">Select a catalog</option>';
        allCatalogs
            .filter(catalog => !projectSlug || catalog.project.slug === projectSlug)
            .forEach(catalog => {
                const option = document.createElement("option");
                option.value = catalog.id;
                option.textContent = catalog.name;
                option.dataset.slug = catalog.slug;
                catalogSelect.appendChild(option);
            });
    }

    projectSelect.addEventListener("change", () => {
        const selectedProjectSlug = projectSelect.value;
        populateCatalogs(selectedProjectSlug);
    });
});

window.submitEventForm = function () {
    const name = document.getElementById("name").value.trim();
    const projectSelect = document.getElementById("projectSelect");
    const catalogSelect = document.getElementById("catalogSelect");
    const projectSlug = projectSelect.value;
    const catalogId = parseInt(catalogSelect.value);
    const selectedOption = catalogSelect.selectedOptions[0];
    const catalogSlug = selectedOption?.dataset.slug;
    const parametersInput = document.getElementById("parameters").value.trim();
    const localCreatedAtRaw = document.getElementById("localCreatedAt").value;

    if (!name || !projectSlug || isNaN(catalogId) || !catalogSlug || !localCreatedAtRaw) {
        alert("Name, Project, Catalog, and Created At are required");
        return;
    }

    let parameters = {};
    if (parametersInput) {
        try {
            parameters = JSON.parse(parametersInput);
        } catch (e) {
            alert("Parameters must be valid JSON");
            return;
        }
    }

    // Форматируем дату в yyyy-MM-ddTHH:mm:ss
    const localCreatedAt = localCreatedAtRaw + ":00";

    const newEvent = {
        name,
        catalogId,
        parameters,
        localCreatedAt
    };

    fetch(`/api/v3/${projectSlug}/${catalogSlug}/events`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newEvent)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to create event: ${response.status}`);
            }
            alert("Event created successfully");

            // Reset form
            document.getElementById("name").value = "";
            projectSelect.value = "";
            catalogSelect.innerHTML = '<option value="">Select a catalog</option>';
            document.getElementById("parameters").value = "";
            const now = new Date();
            document.getElementById("localCreatedAt").value = now.toISOString().slice(0, 16);
        })
        .catch(error => {
            console.error("Error creating event:", error);
            alert("Failed to create event");
        });
};

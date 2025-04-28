document.addEventListener("DOMContentLoaded", () => {
    const localCreatedAtInput = document.getElementById("localCreatedAt");
    const now = new Date();
    localCreatedAtInput.value = now.toISOString().slice(0, 16); // yyyy-MM-ddTHH:mm
});

window.submitEventForm = function () {
    const name = document.getElementById("name").value.trim();
    const projectSlug = document.getElementById("projectSlug").value.trim();
    const catalogSlug = document.getElementById("catalogSlug").value.trim();
    const parametersInput = document.getElementById("parameters").value.trim();
    const localCreatedAtRaw = document.getElementById("localCreatedAt").value;

    if (!name || !projectSlug || !catalogSlug || !localCreatedAtRaw) {
        alert("Name, Project Slug, Catalog Slug, and Created At are required");
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

    const localCreatedAt = localCreatedAtRaw + ":00";

    const newEvent = {
        name,
        catalogSlug,
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

            document.getElementById("name").value = "";
            document.getElementById("projectSlug").value = "";
            document.getElementById("catalogSlug").value = "";
            document.getElementById("parameters").value = "";
            const now = new Date();
            document.getElementById("localCreatedAt").value = now.toISOString().slice(0, 16);
        })
        .catch(error => {
            console.error("Error creating event:", error);
            alert("Failed to create event");
        });
};
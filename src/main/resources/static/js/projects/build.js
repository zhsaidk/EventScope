document.getElementById("projectForm").addEventListener("submit", function(event) {
    event.preventDefault(); // Prevent default form submission to handle it manually if needed

    const form = event.target;
    const formData = new FormData(form);

    fetch(form.action, {
        method: form.method,
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Failed to create project: ${response.status}`);
            }
            alert("Project created successfully");
            window.location.href = "/projects"; // Redirect to projects page
        })
        .catch(error => {
            console.error("Error creating project:", error);
            alert("Failed to create project");
        });
});
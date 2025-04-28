document.addEventListener("DOMContentLoaded", () => {
    // Store projects for accessing projectSlug later
    let projects = [];
    let currentPage = 0;
    let totalPages = 1;

    // Функция для загрузки проектов
    function loadProjects(page = 0) {
        fetch(`/api/v3/projects?page=${page}`)
            .then(response => response.json())
            .then(data => {
                projects = data.content; // Store projects for createEvent
                currentPage = data.page.number;
                totalPages = data.page.totalPages;

                // Очищаем таблицу
                const tbody = document.getElementById("projectsBody");
                tbody.innerHTML = "";

                // Заполняем таблицу
                data.content.forEach(project => {
                    const row = document.createElement("tr");
                    row.setAttribute("data-slug", project.slug);
                    row.innerHTML = `
                        <td>${project.id}</td>
                        <td>${project.name}</td>
                        <td>${project.description || ""}</td>
                        <td>${project.active}</td>
                        <td>${new Date(project.createdAt).toLocaleString()}</td>
                        <td>
                            <button onclick="viewProject('${project.slug}')" class="view-button">View</button>
                            <button onclick="deleteProject('${project.slug}')" class="delete-button">Delete</button>
                        </td>
                        <td>
                            <button onclick="createCatalog('${project.slug}')" class="create-catalog-button">Create Catalog</button>
                        </td>
                    `;
                    tbody.appendChild(row);
                });

                // Обновляем пагинацию
                updatePagination();
            })
            .catch(error => console.error("Error fetching projects:", error));
    }

    // Обновление состояния кнопок пагинации
    function updatePagination() {
        const prevButton = document.getElementById("prevPage");
        const nextButton = document.getElementById("nextPage");
        const pageInfo = document.getElementById("pageInfo");
        const pageInput = document.getElementById("pageInput");

        prevButton.disabled = currentPage === 0;
        nextButton.disabled = currentPage >= totalPages - 1;
        pageInfo.textContent = `Page ${currentPage + 1} of ${totalPages}`;
        pageInput.value = currentPage + 1; // Показываем номер страницы (1-based)
        pageInput.max = totalPages; // Ограничиваем максимальный ввод
    }

    // Загружаем первую страницу
    loadProjects();

    // Обработчики кнопок пагинации
    document.getElementById("prevPage").addEventListener("click", () => {
        if (currentPage > 0) {
            loadProjects(currentPage - 1);
        }
    });

    document.getElementById("nextPage").addEventListener("click", () => {
        if (currentPage < totalPages - 1) {
            loadProjects(currentPage + 1);
        }
    });

    // Обработчик ввода номера страницы
    document.getElementById("pageInput").addEventListener("change", (e) => {
        const page = parseInt(e.target.value, 10);
        if (!isNaN(page) && page >= 1 && page <= totalPages) {
            loadProjects(page - 1); // Переводим в 0-based для API
        } else {
            e.target.value = currentPage + 1; // Возвращаем текущую страницу
            alert("Please enter a valid page number between 1 and " + totalPages);
        }
    });

    // Переход на страницу проекта по slug
    window.viewProject = function(slug) {
        window.location.href = `/projects/${slug}`;
    };

    // Удаление проекта по slug
    window.deleteProject = function(slug) {
        if (confirm("Are you sure you want to delete this project?")) {
            fetch(`/api/v3/${slug}`, {
                method: "DELETE"
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Failed to delete project: ${response.status}`);
                    }
                    const row = document.querySelector(`tr[data-slug="${slug}"]`);
                    if (row) row.remove();
                    alert("Project deleted successfully");
                })
                .catch(error => {
                    console.error("Error deleting project:", error);
                    alert("Failed to delete project");
                });
        }
    };

    // Переход на страницу создания каталога с projectSlug
    window.createCatalog = function(projectSlug) {
        window.location.href = `/projects/catalogs/build?projectSlug=${encodeURIComponent(projectSlug)}`;
    };
});
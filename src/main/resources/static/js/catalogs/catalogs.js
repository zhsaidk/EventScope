const { useState, useEffect } = React;

const App = () => {
    const [catalogs, setCatalogs] = useState([]);
    const [selectedProject, setSelectedProject] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    useEffect(() => {
        fetchCatalogs(currentPage);
    }, [currentPage]);

    const fetchCatalogs = (page) => {
        fetch(`/api/v3/projects/catalogs?page=${page}`)
            .then(response => response.json())
            .then(data => {
                setCatalogs(data.content);
                setTotalPages(data.page.totalPages);
                setCurrentPage(data.page.number);
            })
            .catch(error => console.error('Error fetching catalogs:', error));
    };

    const handleDelete = (projectSlug, catalogSlug) => {
        if (window.confirm('Are you sure you want to delete this catalog?')) {
            fetch(`/api/v3/${projectSlug}/${catalogSlug}`, { method: 'DELETE' })
                .then(response => {
                    if (response.ok) {
                        setCatalogs(catalogs.filter(c => c.slug !== catalogSlug));
                    } else {
                        alert('Failed to delete catalog');
                    }
                })
                .catch(error => console.error('Error deleting catalog:', error));
        }
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
        }
    };

    const handlePageInput = (e) => {
        const page = parseInt(e.target.value, 10);
        if (!isNaN(page) && page >= 1 && page <= totalPages) {
            setCurrentPage(page - 1); // API uses 0-based indexing
        } else {
            alert(`Please enter a valid page number between 1 and ${totalPages}`);
        }
    };

    const getProjectSlugForCreate = () => {
        if (selectedProject) {
            return encodeURIComponent(selectedProject.slug);
        }
        if (catalogs.length > 0) {
            return encodeURIComponent(catalogs[0].project.slug);
        }
        return 'default-project-slug';
    };

    const renderCatalogTable = () => (
        <div className="table-container">
            <table>
                <thead>
                <tr>
                    <th>Id</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Active</th>
                    <th>Version</th>
                    <th>Created At</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {catalogs.map(catalog => (
                    <tr key={catalog.slug}>
                        <td>{catalog.id}</td>
                        <td>{catalog.name}</td>
                        <td>{catalog.description}</td>
                        <td>{catalog.active ? 'Yes' : 'No'}</td>
                        <td>{catalog.version || 'N/A'}</td>
                        <td>{new Date(catalog.createdAt).toLocaleString()}</td>
                        <td>
                            <button
                                className="view-btn"
                                onClick={() => setSelectedProject(catalog.project)}
                            >
                                View Project
                            </button>
                            <button
                                className="details-btn"
                                onClick={() => window.location.href = `/projects/catalogs/${encodeURIComponent(catalog.slug)}?projectSlug=${encodeURIComponent(catalog.project.slug)}`}
                            >
                                Details
                            </button>
                            <button
                                className="delete-btn"
                                onClick={() => handleDelete(catalog.project.slug, catalog.slug)}
                            >
                                Delete
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            <div className="pagination-container">
                <button
                    className="pagination-btn"
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                >
                    Previous
                </button>
                <span className="page-info">Page {currentPage + 1} of {totalPages}</span>
                <label htmlFor="pageInput">Go to page: </label>
                <input
                    id="pageInput"
                    type="number"
                    min="1"
                    max={totalPages}
                    value={currentPage + 1}
                    onChange={handlePageInput}
                    className="page-input"
                />
                <button
                    className="pagination-btn"
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                >
                    Next
                </button>
            </div>
            <button
                className="create-btn"
                onClick={() => window.location.href = `/projects`}
            >
                Back
            </button>
        </div>
    );

    const renderProjectTable = () => (
        <div>
            <h2 className="text-xl font-bold mb-2">Project Details</h2>
            <table>
                <thead>
                <tr>
                    <th>Slug</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Active</th>
                    <th>Created At</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>{selectedProject.slug}</td>
                    <td>{selectedProject.name}</td>
                    <td>{selectedProject.description || 'N/A'}</td>
                    <td>{selectedProject.active ? 'Yes' : 'No'}</td>
                    <td>{new Date(selectedProject.createdAt).toLocaleString()}</td>
                </tr>
                </tbody>
            </table>
            <button
                className="back-btn"
                onClick={() => setSelectedProject(null)}
            >
                Back to Catalogs
            </button>
        </div>
    );

    return (
        <div className="container">
            <h1>Catalogs</h1>
            {selectedProject ? renderProjectTable() : renderCatalogTable()}
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
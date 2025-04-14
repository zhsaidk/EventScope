const { useState, useEffect } = React;

const App = () => {
    const [catalogs, setCatalogs] = useState([]);
    const [selectedProject, setSelectedProject] = useState(null);

    useEffect(() => {
        fetch('/api/v3/projects/catalogs')
            .then(response => response.json())
            .then(data => setCatalogs(data))
            .catch(error => console.error('Error fetching catalogs:', error));
    }, []);

    const handleDelete = (id) => {
        if (window.confirm('Are you sure you want to delete this catalog?')) {
            fetch(`/api/v3/projects/catalogs/${id}`, { method: 'DELETE' })
                .then(response => {
                    if (response.ok) {
                        setCatalogs(catalogs.filter(catalog => catalog.id !== id));
                    } else {
                        alert('Failed to delete catalog');
                    }
                })
                .catch(error => console.error('Error deleting catalog:', error));
        }
    };

    const renderCatalogTable = () => (
        <div className="table-container">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
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
                    <tr key={catalog.id}>
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
                                onClick={() => window.location.href = `/projects/catalogs/${catalog.id}`}
                            >
                                Details
                            </button>
                            <button
                                className="delete-btn"
                                onClick={() => handleDelete(catalog.id)}
                            >
                                Delete
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            <button
                className="create-btn"
                onClick={() => window.location.href = '/projects/catalogs/build'}
            >
                Create New Catalog
            </button>
        </div>
    );

    const renderProjectTable = () => (
        <div>
            <h2 className="text-xl font-bold mb-2">Project Details</h2>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Active</th>
                    <th>Created At</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>{selectedProject.id}</td>
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
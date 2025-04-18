const { useState, useEffect } = React;

const App = () => {
    const [catalog, setCatalog] = useState({ name: '', description: '', active: true, version: '' });
    const [error, setError] = useState(null);

    // Получаем catalogSlug из пути и projectSlug из query string
    const pathParts = window.location.pathname.split('/');
    const catalogSlug = pathParts[pathParts.length - 1]; // Последняя часть пути: catalogSlug
    const params = new URLSearchParams(window.location.search);
    const projectSlug = params.get('projectSlug'); // Извлекаем projectSlug из ?projectSlug=...

    useEffect(() => {
        if (!catalogSlug) {
            setError('Catalog slug is missing in URL');
            return;
        }
        if (!projectSlug) {
            setError('Project slug is missing in URL query');
            return;
        }

        fetch(`/api/v3/${projectSlug}/${catalogSlug}`)
            .then(response => {
                if (!response.ok) throw new Error('Catalog not found');
                return response.json();
            })
            .then(data => setCatalog({
                name: data.name,
                description: data.description,
                active: data.active,
                version: data.version || ''
            }))
            .catch(err => setError(err.message));
    }, [catalogSlug, projectSlug]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCatalog(prev => ({ ...prev, [name]: value }));
    };

    const handleActiveChange = (e) => {
        setCatalog(prev => ({ ...prev, active: e.target.value === 'true' }));
    };

    const handleUpdate = () => {
        if (!catalogSlug || !projectSlug) {
            setError('Catalog slug or project slug is missing');
            return;
        }

        fetch(`/api/v3/${projectSlug}/${catalogSlug}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(catalog)
        })
            .then(response => {
                if (!response.ok) throw new Error('Failed to update catalog');
                alert('Catalog updated successfully');
            })
            .catch(err => setError(err.message));
    };

    if (error) {
        return <div className="container"><div className="error">{error}</div></div>;
    }

    return (
        <div className="container">
            <h1>Catalog Details</h1>
            <div className="form-group">
                <label>Name</label>
                <input
                    type="text"
                    name="name"
                    value={catalog.name}
                    onChange={handleInputChange}
                />
            </div>
            <div className="form-group">
                <label>Description</label>
                <textarea
                    name="description"
                    value={catalog.description}
                    onChange={handleInputChange}
                ></textarea>
            </div>
            <div className="form-group">
                <label>Active</label>
                <select
                    name="active"
                    value={catalog.active}
                    onChange={handleActiveChange}
                >
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                </select>
            </div>
            <div className="form-group">
                <label>Version</label>
                <input
                    type="text"
                    name="version"
                    value={catalog.version}
                    onChange={handleInputChange}
                />
            </div>
            <button className="update-btn" onClick={handleUpdate}>
                Update Catalog
            </button>
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
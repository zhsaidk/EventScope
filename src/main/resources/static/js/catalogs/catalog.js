const { useState, useEffect } = React;

const App = () => {
    const [catalog, setCatalog] = useState({ name: '', description: '', active: true, version: '' });
    const [error, setError] = useState(null);
    const catalogId = window.location.pathname.split('/').pop();

    useEffect(() => {
        fetch(`/api/v3/projects/catalogs/${catalogId}`)
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
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCatalog(prev => ({ ...prev, [name]: value }));
    };

    const handleActiveChange = (e) => {
        setCatalog(prev => ({ ...prev, active: e.target.value === 'true' }));
    };

    const handleUpdate = () => {
        fetch(`/api/v3/projects/catalogs/${catalogId}`, {
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
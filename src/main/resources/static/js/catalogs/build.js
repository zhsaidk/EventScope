const { useState } = React;

const App = () => {
    const [catalog, setCatalog] = useState({ name: '', description: '', projectId: '', active: true, version: '' });
    const [error, setError] = useState(null);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCatalog(prev => ({ ...prev, [name]: name === 'projectId' ? parseInt(value) || '' : value }));
    };

    const handleActiveChange = (e) => {
        setCatalog(prev => ({ ...prev, active: e.target.value === 'true' }));
    };

    const handleCreate = () => {
        if (!catalog.name || !catalog.description || !catalog.projectId || !catalog.version) {
            setError('All fields are required');
            return;
        }

        fetch('/api/v3/projects/catalogs/build', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(catalog)
        })
            .then(response => {
                if (!response.ok) throw new Error('Failed to create catalog');
                alert('Catalog created successfully');
                setCatalog({ name: '', description: '', projectId: '', active: true, version: '' });
                setError(null);
            })
            .catch(err => setError(err.message));
    };

    return (
        <div className="container">
            <h1>Create Catalog</h1>
            {error && <div className="error">{error}</div>}
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
                <label>Project ID</label>
                <input
                    type="number"
                    name="projectId"
                    value={catalog.projectId}
                    onChange={handleInputChange}
                />
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
            <button className="create-btn" onClick={handleCreate}>
                Create Catalog
            </button>
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
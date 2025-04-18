const { useState, useEffect } = React;

const App = () => {
    const [catalog, setCatalog] = useState({ name: '', description: '', version: '', active: true });
    const [error, setError] = useState(null);
    const [projectSlug, setProjectSlug] = useState('');

    // Извлекаем projectSlug из параметров URL при монтировании компонента
    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const slug = params.get('projectSlug');
        if (slug) {
            setProjectSlug(slug);
        } else {
            setError('Project slug is missing in URL');
        }
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCatalog(prev => ({ ...prev, [name]: value }));
    };

    const handleActiveChange = (e) => {
        setCatalog(prev => ({ ...prev, active: e.target.value === 'true' }));
    };

    const handleCreate = () => {
        if (!projectSlug || !catalog.name || !catalog.description || !catalog.version) {
            setError('All fields are required');
            return;
        }

        fetch(`/api/v3/${projectSlug}/catalogs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(catalog)
        })
            .then(response => {
                if (!response.ok) throw new Error('Failed to create catalog');
                alert('Catalog created successfully');
                setCatalog({ name: '', description: '', version: '', active: true });
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
const { useState, useEffect } = React;

const App = () => {
    const [event, setEvent] = useState(null);
    const [showCatalog, setShowCatalog] = useState(false);
    const [error, setError] = useState(null);
    const eventId = window.location.pathname.split('/').pop();

    useEffect(() => {
        fetch(`/api/v3/projects/catalogs/events/${eventId}`)
            .then(response => {
                if (!response.ok) throw new Error('Event not found');
                return response.json();
            })
            .then(data => setEvent(data))
            .catch(err => setError(err.message));
    }, []);

    const handleDelete = () => {
        if (window.confirm('Are you sure you want to delete this event?')) {
            fetch(`/api/v3/projects/catalogs/events/${eventId}`, { method: 'DELETE' })
                .then(response => {
                    if (response.ok) {
                        alert('Event deleted successfully');
                        window.location.href = '/projects/catalogs';
                    } else {
                        throw new Error('Failed to delete event');
                    }
                })
                .catch(err => setError(err.message));
        }
    };

    const renderEventTable = () => (
        <div className="table-container">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Local Created At</th>
                    <th>Created At</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>{event.id}</td>
                    <td>{event.name}</td>
                    <td>{event.localCreatedAt ? new Date(event.localCreatedAt).toLocaleString() : 'N/A'}</td>
                    <td>{event.createdAt ? new Date(event.createdAt).toLocaleString() : 'N/A'}</td>
                    <td>
                        <button
                            className="view-btn"
                            onClick={() => setShowCatalog(!showCatalog)}
                        >
                            {showCatalog ? 'Hide Catalog' : 'View Catalog'}
                        </button>
                        <button
                            className="delete-btn"
                            onClick={handleDelete}
                        >
                            Delete
                        </button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    );

    const renderCatalogTable = () => (
        <div className="table-container">
            <h2>Catalog Details</h2>
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Active</th>
                    <th>Version</th>
                    <th>Created At</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>{event.catalog.id}</td>
                    <td>{event.catalog.name}</td>
                    <td>{event.catalog.description}</td>
                    <td>{event.catalog.active ? 'Yes' : 'No'}</td>
                    <td>{event.catalog.version || 'N/A'}</td>
                    <td>{event.catalog.createdAt ? new Date(event.catalog.createdAt).toLocaleString() : 'N/A'}</td>
                </tr>
                </tbody>
            </table>
        </div>
    );

    if (error) {
        return <div className="container"><div className="error">{error}</div></div>;
    }

    if (!event) {
        return <div className="container">Loading...</div>;
    }

    return (
        <div className="container">
            <h1>Event Details</h1>
            {renderEventTable()}
            {showCatalog && renderCatalogTable()}
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
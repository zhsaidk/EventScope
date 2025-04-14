const { useState } = React;

const App = () => {
    const [filter, setFilter] = useState({ name: '', begin: '', end: '' });
    const [events, setEvents] = useState(null);
    const [error, setError] = useState(null);
    const [showCatalogs, setShowCatalogs] = useState({});
    const [editingEvent, setEditingEvent] = useState(null);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFilter(prev => ({ ...prev, [name]: value }));
    };

    const handleSearch = () => {
        const payload = {
            name: filter.name || "" // Отправляем пустую строку, если name не заполнено
        };
        if (filter.begin) payload.begin = new Date(filter.begin).toISOString();
        if (filter.end) payload.end = new Date(filter.end).toISOString();

        fetch('/api/v3/projects/catalogs/events/search', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
            .then(response => {
                if (!response.ok) throw new Error('Failed to fetch events');
                return response.json();
            })
            .then(data => {
                setEvents(data);
                setShowCatalogs({});
                setEditingEvent(null);
                setError(null);
            })
            .catch(err => {
                setEvents(null);
                setError(err.message);
            });
    };

    const handleDelete = (id) => {
        if (window.confirm('Are you sure you want to delete this event?')) {
            fetch(`/api/v3/projects/catalogs/events/${id}`, { method: 'DELETE' })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to delete event');
                    setEvents(events.filter(event => event.id !== id));
                })
                .catch(err => setError(err.message));
        }
    };

    const handleEdit = (id) => {
        fetch(`/api/v3/projects/catalogs/events/${id}`)
            .then(response => {
                if (!response.ok) throw new Error('Failed to fetch event');
                return response.json();
            })
            .then(data => {
                setEditingEvent({
                    id,
                    name: data.name || '',
                    parameters: JSON.stringify(data.parameters || {}, null, 2),
                    localCreatedAt: data.localCreatedAt ? new Date(data.localCreatedAt).toISOString().slice(0, 16) : ''
                });
            })
            .catch(err => setError(err.message));
    };

    const handleUpdate = () => {
        let parsedParameters;
        try {
            parsedParameters = editingEvent.parameters ? JSON.parse(editingEvent.parameters) : {};
        } catch (e) {
            setError('Invalid JSON in parameters');
            return;
        }

        const payload = {
            name: editingEvent.name,
            parameters: parsedParameters,
            localCreatedAt: new Date(editingEvent.localCreatedAt).toISOString()
        };

        fetch(`/api/v3/projects/catalogs/events/${editingEvent.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
            .then(response => {
                if (!response.ok) throw new Error('Failed to update event');
                setEvents(events.map(event =>
                    event.id === editingEvent.id
                        ? { ...event, ...payload }
                        : event
                ));
                setEditingEvent(null);
                alert('Event updated successfully');
            })
            .catch(err => setError(err.message));
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditingEvent(prev => ({ ...prev, [name]: value }));
    };

    const renderFilterForm = () => (
        <div className="filter-form">
            <div className="form-group">
                <label>Name</label>
                <input
                    type="text"
                    name="name"
                    value={filter.name}
                    onChange={handleInputChange}
                />
            </div>
            <div className="form-group">
                <label>Begin Date</label>
                <input
                    type="datetime-local"
                    name="begin"
                    value={filter.begin}
                    onChange={handleInputChange}
                />
            </div>
            <div className="form-group">
                <label>End Date</label>
                <input
                    type="datetime-local"
                    name="end"
                    value={filter.end}
                    onChange={handleInputChange}
                />
            </div>
            <button className="search-btn" onClick={handleSearch}>
                Search Events
            </button>
            <button
                className="create-btn"
                onClick={() => window.location.href = '/projects/catalogs/events/build'}
            >
                Create Event
            </button>
        </div>
    );

    const renderEditForm = () => (
        <div className="edit-form">
            <h2>Edit Event</h2>
            <div className="form-group">
                <label>Name</label>
                <input
                    type="text"
                    name="name"
                    value={editingEvent.name}
                    onChange={handleEditChange}
                />
            </div>
            <div className="form-group">
                <label>Parameters (JSON)</label>
                <textarea
                    name="parameters"
                    value={editingEvent.parameters}
                    onChange={handleEditChange}
                ></textarea>
            </div>
            <div className="form-group">
                <label>Local Created At</label>
                <input
                    type="datetime-local"
                    name="localCreatedAt"
                    value={editingEvent.localCreatedAt}
                    onChange={handleEditChange}
                />
            </div>
            <button className="update-btn" onClick={handleUpdate}>
                Update Event
            </button>
            <button className="cancel-btn" onClick={() => setEditingEvent(null)}>
                Cancel
            </button>
        </div>
    );

    const renderEventsTable = () => (
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
                {events.map(event => (
                    <React.Fragment key={event.id}>
                        <tr>
                            <td>{event.id}</td>
                            <td>{event.name || 'N/A'}</td>
                            <td>{event.localCreatedAt ? new Date(event.localCreatedAt).toLocaleString() : 'N/A'}</td>
                            <td>{event.createdAt ? new Date(event.createdAt).toLocaleString() : 'N/A'}</td>
                            <td>
                                <button
                                    className="details-btn"
                                    onClick={() => setShowCatalogs(prev => ({
                                        ...prev,
                                        [event.id]: !prev[event.id]
                                    }))}
                                >
                                    {showCatalogs[event.id] ? 'Hide Catalog' : 'Show Catalog'}
                                </button>
                                <button
                                    className="edit-btn"
                                    onClick={() => handleEdit(event.id)}
                                >
                                    Edit
                                </button>
                                <button
                                    className="delete-btn"
                                    onClick={() => handleDelete(event.id)}
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                        {showCatalogs[event.id] && (
                            <tr>
                                <td colSpan="5">
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
                                </td>
                            </tr>
                        )}
                    </React.Fragment>
                ))}
                </tbody>
            </table>
        </div>
    );

    return (
        <div className="container">
            <h1>Events</h1>
            {renderFilterForm()}
            {error && <div className="error">{error}</div>}
            {editingEvent && renderEditForm()}
            {events && events.length > 0 ? renderEventsTable() : events && <div className="error">No events found</div>}
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
const { useState, useEffect } = React;

const App = () => {
    const [filter, setFilter] = useState({ name: '', begin: '', end: '' });
    const [events, setEvents] = useState(null); // Изначально null, чтобы указать, что данные еще не загружены
    const [error, setError] = useState(null);
    const [showCatalogs, setShowCatalogs] = useState({});
    const [editingEvent, setEditingEvent] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    // Загружаем события при монтировании компонента
    useEffect(() => {
        fetchEvents(0);
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFilter(prev => ({ ...prev, [name]: value }));
    };

    const fetchEvents = (page = 0) => {
        const params = new URLSearchParams();
        params.append('name', filter.name || '');
        if (filter.begin) params.append('begin', new Date(filter.begin).toISOString());
        if (filter.end) params.append('end', new Date(filter.end).toISOString());
        params.append('page', page);

        fetch(`/api/v3/projects/catalogs/events?${params.toString()}`, {
            method: 'GET'
        })
            .then(response => {
                if (!response.ok) throw new Error('Failed to fetch events');
                return response.json();
            })
            .then(data => {
                setEvents(data.content);
                setTotalPages(data.page.totalPages);
                setCurrentPage(data.page.number);
                setShowCatalogs({});
                setEditingEvent(null);
                setError(null);
            })
            .catch(err => {
                setEvents([]);
                setError(err.message);
            });
    };

    const handleSearch = () => {
        setCurrentPage(0); // Сбрасываем на первую страницу при новом поиске
        fetchEvents(0);
    };

    const handleDelete = (id) => {
        if (window.confirm('Are you sure you want to delete this event?')) {
            const event = events.find(event => event.id === id);
            if (!event || !event.catalog || !event.catalog.project || !event.catalog.project.slug || !event.catalog.slug) {
                setError('Missing projectSlug or catalogSlug for deletion');
                return;
            }

            fetch(`/api/v3/${event.catalog.project.slug}/${event.catalog.slug}/${id}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to delete event');
                    setEvents(events.filter(event => event.id !== id));
                })
                .catch(err => setError(err.message));
        }
    };

    const handleEdit = (id) => {
        const event = events.find(event => event.id === id);
        if (!event || !event.catalog || !event.catalog.project || !event.catalog.project.slug || !event.catalog.slug) {
            setError('Missing projectSlug or catalogSlug for fetching event');
            return;
        }

        fetch(`/api/v3/${event.catalog.project.slug}/${event.catalog.slug}/${id}`)
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
                setError(null);
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

        const event = events.find(event => event.id === editingEvent.id);
        if (!event || !event.catalog || !event.catalog.project || !event.catalog.project.slug || !event.catalog.slug) {
            setError('Missing projectSlug or catalogSlug for update');
            return;
        }

        const payload = {
            name: editingEvent.name,
            parameters: parsedParameters,
            localCreatedAt: new Date(editingEvent.localCreatedAt).toISOString()
        };

        fetch(`/api/v3/${event.catalog.project.slug}/${event.catalog.slug}/${editingEvent.id}`, {
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

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
            fetchEvents(newPage);
        }
    };

    const handlePageInput = (e) => {
        const page = parseInt(e.target.value, 10);
        if (!isNaN(page) && page >= 1 && page <= totalPages) {
            setCurrentPage(page - 1); // API использует 0-based индексацию
            fetchEvents(page - 1);
        } else {
            alert(`Please enter a valid page number between 1 and ${totalPages}`);
        }
    };

    const renderFilterForm = () => (
        <div className="filter-form p-6 bg-white rounded-lg shadow-md mb-6">
            <div className="flex flex-col md:flex-row md:space-x-4">
                <div className="form-group flex-1">
                    <label className="block text-gray-700 font-semibold mb-2">Name</label>
                    <input
                        type="text"
                        name="name"
                        value={filter.name}
                        onChange={handleInputChange}
                        className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div className="form-group flex-1">
                    <label className="block text-gray-700 font-semibold mb-2">Begin Date</label>
                    <input
                        type="datetime-local"
                        name="begin"
                        value={filter.begin}
                        onChange={handleInputChange}
                        className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <div className="form-group flex-1">
                    <label className="block text-gray-700 font-semibold mb-2">End Date</label>
                    <input
                        type="datetime-local"
                        name="end"
                        value={filter.end}
                        onChange={handleInputChange}
                        className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
            </div>
            <div className="mt-4 flex space-x-4">
                <button
                    className="search-btn bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition"
                    onClick={handleSearch}
                >
                    Search Events
                </button>
                <button
                    className="create-btn bg-green-500 text-white px-4 py-2 rounded-md hover:bg-green-600 transition"
                    onClick={() => window.location.href = '/projects/catalogs/events/build'}
                >
                    Create Event
                </button>
            </div>
        </div>
    );

    const renderEditForm = () => (
        <div className="edit-form p-6 bg-white rounded-lg shadow-md mb-6">
            <h2 className="text-2xl font-bold mb-4">Edit Event</h2>
            <div className="form-group mb-4">
                <label className="block text-gray-700 font-semibold mb-2">Name</label>
                <input
                    type="text"
                    name="name"
                    value={editingEvent.name}
                    onChange={handleEditChange}
                    className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
            </div>
            <div className="form-group mb-4">
                <label className="block text-gray-700 font-semibold mb-2">Parameters (JSON)</label>
                <textarea
                    name="parameters"
                    value={editingEvent.parameters}
                    onChange={handleEditChange}
                    className="w-full p-2 border rounded-md h-32 focus:outline-none focus:ring-2 focus:ring-blue-500"
                ></textarea>
            </div>
            <div className="form-group mb-4">
                <label className="block text-gray-700 font-semibold mb-2">Local Created At</label>
                <input
                    type="datetime-local"
                    name="localCreatedAt"
                    value={editingEvent.localCreatedAt}
                    onChange={handleEditChange}
                    className="w-full p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
            </div>
            <div className="flex space-x-4">
                <button
                    className="update-btn bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition"
                    onClick={handleUpdate}
                >
                    Update Event
                </button>
                <button
                    className="cancel-btn bg-gray-500 text-white px-4 py-2 rounded-md hover:bg-gray-600 transition"
                    onClick={() => setEditingEvent(null)}
                >
                    Cancel
                </button>
            </div>
        </div>
    );

    const renderEventsTable = () => (
        <div className="table-container bg-white rounded-lg shadow-md p-6">
            <table className="w-full table-auto">
                <thead>
                <tr className="bg-gray-200">
                    <th className="px-4 py-2 text-left">ID</th>
                    <th className="px-4 py-2 text-left">Name</th>
                    <th className="px-4 py-2 text-left">Local Created At</th>
                    <th className="px-4 py-2 text-left">Created At</th>
                    <th className="px-4 py-2 text-left">Actions</th>
                </tr>
                </thead>
                <tbody>
                {events.map(event => (
                    <React.Fragment key={event.id}>
                        <tr className="border-b">
                            <td className="px-4 py-2">{event.id}</td>
                            <td className="px-4 py-2">{event.name || 'N/A'}</td>
                            <td className="px-4 py-2">{event.localCreatedAt ? new Date(event.localCreatedAt).toLocaleString() : 'N/A'}</td>
                            <td className="px-4 py-2">{event.createdAt ? new Date(event.createdAt).toLocaleString() : 'N/A'}</td>
                            <td className="px-4 py-2 flex space-x-2">
                                <button
                                    className="details-btn bg-purple-500 text-white px-3.reset py-1 rounded-md hover:bg-purple-600 transition"
                                    onClick={() => setShowCatalogs(prev => ({
                                        ...prev,
                                        [event.id]: !prev[event.id]
                                    }))}
                                >
                                    {showCatalogs[event.id] ? 'Hide Catalog' : 'Show Catalog'}
                                </button>
                                <button
                                    className="edit-btn bg-yellow-500 text-white px-3 py-1 rounded-md hover:bg-yellow-600 transition"
                                    onClick={() => handleEdit(event.id)}
                                >
                                    Edit
                                </button>
                                <button
                                    className="delete-btn bg-red-500 text-white px-3 py-1 rounded-md hover:bg-red-600 transition"
                                    onClick={() => handleDelete(event.id)}
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                        {showCatalogs[event.id] && (
                            <tr>
                                <td colSpan="5" className="px-4 py-2">
                                    <div className="table-container bg-gray-50 p-4 rounded-md">
                                        <table className="w-full table-auto">
                                            <thead>
                                            <tr className="bg-gray-100">
                                                <th className="px-4 py-2 text-left">ID</th>
                                                <th className="px-4 py-2 text-left">Name</th>
                                                <th className="px-4 py-2 text-left">Description</th>
                                                <th className="px-4 py-2 text-left">Active</th>
                                                <th className="px-4 py-2 text-left">Version</th>
                                                <th className="px-4 py-2 text-left">Created At</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr>
                                                <td className="px-4 py-2">{event.catalog.id}</td>
                                                <td className="px-4 py-2">{event.catalog.name}</td>
                                                <td className="px-4 py-2">{event.catalog.description}</td>
                                                <td className="px-4 py-2">{event.catalog.active ? 'Yes' : 'No'}</td>
                                                <td className="px-4 py-2">{event.catalog.version || 'N/A'}</td>
                                                <td className="px-4 py-2">{event.catalog.createdAt ? new Date(event.catalog.createdAt).toLocaleString() : 'N/A'}</td>
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
            <div className="pagination-container flex justify-center items-center mt-6 space-x-4">
                <button
                    className="pagination-btn bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 0}
                >
                    Previous
                </button>
                <span className="page-info text-gray-700">Page {currentPage + 1} of {totalPages}</span>
                <div className="flex items-center">
                    <label htmlFor="pageInput" className="mr-2 text-gray-700">Go to page:</label>
                    <input
                        id="pageInput"
                        type="number"
                        min="1"
                        max={totalPages}
                        value={currentPage + 1}
                        onChange={handlePageInput}
                        className="w-16 p-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <button
                    className="pagination-btn bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages - 1}
                >
                    Next
                </button>
            </div>
        </div>
    );

    return (
        <div className="container max-w-7xl mx-auto p-6">
            <h1 className="text-3xl font-bold mb-6">Events</h1>
            {renderFilterForm()}
            {error && <div className="error bg-red-100 text-red-700 p-4 rounded-md mb-6">{error}</div>}
            {editingEvent && renderEditForm()}
            {events === null ? (
                <div className="loading bg-blue-100 text-blue-700 p-4 rounded-md mb-6">Loading events...</div>
            ) : events.length > 0 ? (
                renderEventsTable()
            ) : (
                <div className="error bg-yellow-100 text-yellow-700 p-4 rounded-md mb-6">No events found</div>
            )}
        </div>
    );
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
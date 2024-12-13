export const openFile = async (filePath) => {
    try {
        await fetch('http://localhost:8080/files/open', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ filePath }),
        });
    } catch (error) {
        alert('Failed to open the file: ' + error.message);
    }
};

export const deleteFile = async (id) => {
    try {
        const res = await fetch('http://localhost:8080/files/delete', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id }),
        });
        if (!res.ok) {
            throw new Error('Failed to delete file');
        }
    } catch (error) {
        alert('Failed to delete file: ' + error.message);
    }
};

export const handleDelete = async (id, refetch) => {
    try {
        await deleteFile(id);
        refetch();
    } catch (error) {
        console.error('Error deleting file:', error);
    }
};

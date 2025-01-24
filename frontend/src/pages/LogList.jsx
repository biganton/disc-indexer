import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
} from '@mui/material';
import NavHeader from '../components/NavHeader.jsx';

const fetchLogs = async () => {
  const res = await fetch('http://localhost:8080/logs/all');
  if (!res.ok) {
    throw new Error('Failed to fetch logs');
  }
  return res.json();
};

const revertAction = async (id, refetch) => {
  try {
    const res = await fetch('http://localhost:8080/logs/revert', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ id }),
    });
    if (!res.ok) {
      throw new Error('Failed to revert action');
    }
    alert('Action reverted successfully');
    refetch()
  } catch (error) {
    alert(`Error: ${error.message}`);
  }
};

const Logs = () => {
  const { data, error, isLoading, refetch } = useQuery({
    queryKey: ['logs'],
    queryFn: fetchLogs,
  });

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  return (
    <div>
      <NavHeader pageName="Action Logs" />
      <TableContainer component={Paper} style={{ marginBottom: '20px' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Action Type</TableCell>
              <TableCell>File Path</TableCell>
              <TableCell>Target Path</TableCell>
              <TableCell>Timestamp</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Error Message</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data
              .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
              .map((log) => (
                <TableRow key={log.id}>
                  <TableCell>{log.id}</TableCell>
                  <TableCell>{log.actionType}</TableCell>
                  <TableCell>{log.filePath || '-'}</TableCell>
                  <TableCell>{log.targetPath || '-'}</TableCell>
                  <TableCell>
                    {new Date(log.timestamp).toLocaleString()}
                  </TableCell>
                  <TableCell>{log.status}</TableCell>
                  <TableCell>{log.errorMessage || '-'}</TableCell>
                  <TableCell>
                    {['DELETE_FILE', 'MOVE_FILES'].includes(log.actionType) && log.status !== 'REVERTED' && (
                      <Button
                        variant="contained"
                        color="primary"
                        style={{ margin: '10px' }}
                        onClick={() => revertAction(log.id, refetch)}
                      >
                        Revert
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default Logs;

import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/files",
});

export const fetchFiles = async () => {
  const response = await api.get("/all");
  return response.data;
};

export const scanFolder = async (folderPath) => {
  const response = await api.post("/scan", null, {
    params: { folderPath },
  });
  return response.data;
};

export const deleteAllFiles = async () => {
  const response = await api.delete("/deleteAll");
  return response.data;
};

export const fetchDuplicates = async () => {
  const response = await api.get("/duplicates");
  return response.data;
};

export const fetchVersions = async (threshold = 3) => {
  const response = await api.get("/versions", {
    params: { threshold },
  });
  return response.data;
};



# Disc Indexer

Disc Indexer is a Java application designed to organize and manage disk files efficiently. It identifies duplicate files using hashing algorithms and provides tools to archive or delete them, helping to save storage space. The application also features a built-in file indexing system that stores metadata in a database, enabling quick access without the need for repeated calculations.

Disc Indexer allows users to search for content within files by keywords. Simply provide a word, and the application will show which files contain it.

---

## Features

- **Duplicate File Detection**: Identifies and removes duplicate files using hashing algorithms.
- **File Indexing**: Stores file metadata in a MongoDB database for fast retrieval.
- **Content Search**: Enables keyword-based search within files.
- **Space Optimization**: Tools to archive or delete unnecessary files to save space.

---

## Prerequisites

1. **Docker**: Required to run MongoDB in a container.
2. **Java 21**: Ensure your system has OpenJDK 21 installed and properly configured.
3. **Node.js & Vite**: For running the React frontend.

---

## Setup and Installation

### MongoDB Setup

1. **Pull the MongoDB Docker Image**:
   ```bash
   docker pull mongo
   ```

2. **Run the MongoDB Container**:
   ```bash
   docker run --name my-mongo-container -d -p 27017:27017 mongo
   ```

3. **Create the Database and Collection**:
   Use MongoDB Compass or a client tool to create:
    - **Database Name**: `spring-test`
    - **Collection Name**: `files`

---




### Starting the Backend Application

1. Navigate to the Backend Directory:
   ```bash
   cd backend
   ```

2. Build the Application:
   Use Gradle to compile the project if not already built:
   ```bash
   ./gradlew build
   ```

3. Run the Application:
   Use the `java` command to start the application:
   ```bash
   java -jar build/libs/disc-indexer-0.0.1-SNAPSHOT.jar
   ```

4. Open your browser and navigate to the default backend API URL: `http://localhost:8080`


5. To access the Swagger API documentation, visit: `http://localhost:8080/docs`


---

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the Vite development server:
   ```bash
   npm run dev
   ```

4. Open your browser and visit the URL displayed in the terminal (e.g., `http://localhost:5173`).

---

## How to Use

1. Launch both the backend and frontend servers.
2. Access the frontend through your browser.
3. Use the search feature to find files containing specific keywords.
4. Identify duplicate files and choose to archive or delete them.

---

## Troubleshooting

- Ensure Docker is running and the MongoDB container is active on port `27017`.
- Verify the database `spring-test` and collection `files` exist in MongoDB.
- Ensure all required dependencies are installed for both the backend and frontend.

---

## License

This project is licensed under the [MIT License](LICENSE).

---

Happy indexing and organizing!
```

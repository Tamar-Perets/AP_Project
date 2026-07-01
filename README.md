# Advanced Programming Project

## Authors
- Tamar Perets
- Ofek Sharon


Bar-Ilan University  
Advanced Programming Course

---

# Project Overview

This project was developed as the final project of the **Advanced Programming** course.

The system simulates a configurable computational network built from **Topics** and **Agents**. Users can remotely upload a configuration file through a web interface, visualize the generated computational graph, publish messages to topics, and observe how values propagate through the graph.

The project follows an MVC-like architecture and includes a lightweight HTTP server that was implemented entirely in Java without using external web frameworks.

---

# Main Features

- Lightweight multithreaded HTTP server
- Dynamic servlet registration
- HTTP request parsing
- Dynamic configuration loading
- Computational graph generation
- Publish/Subscribe communication model
- Parallel execution of agents
- Reflection-based agent creation
- Dynamic HTML generation
- Interactive graph visualization

---

# Project Architecture

```
               Browser
                  │
                  ▼
          MyHTTPServer
                  │
        ------------------------
        │          │          │
 HtmlLoader  ConfLoader  TopicDisplayer
        │          │
        ▼          ▼
    HTML Files  GenericConfig
                    │
                    ▼
                 Graph
                    │
                    ▼
             TopicManager
                    │
                    ▼
                 Agents
```

The project is divided into three logical layers:

### Model

Contains the computational logic of the system.

Main classes:

- Topic
- Message
- Agent
- ParallelAgent
- GenericConfig
- Graph
- Node

---

### Controller

Responsible for handling HTTP requests and communicating with the model.

Main classes:

- MyHTTPServer
- RequestParser
- HtmlLoader
- TopicDisplayer
- ConfLoader

---

### View

Responsible for displaying the user interface.

Includes:

- index.html
- form.html
- graph.html
- temp.html
- HtmlGraphWriter

---

# Technologies Used

- Java
- Java Threads
- ExecutorService
- Reflection API
- HTML5
- JavaScript
- vis-network
- ConcurrentHashMap
- CopyOnWriteArrayList
- BlockingQueue

---

# Installation

Clone the repository:

Open the project using Eclipse (or any Java IDE).

Make sure the HTML files are located inside the designated HTML directory.

Run the project's **Main** class.

---

# Running the Project

Start the server.

Open your browser and navigate to:

```
http://localhost:8080/app/index.html
```

Using the interface you can:

- Upload a configuration file.
- View the generated computational graph.
- Publish messages to Topics.
- Observe the updated values in the system.

---

# Configuration Files

The computational graph is built dynamically from configuration files.

Each agent is described using three lines:

```
AgentClass
SubscribedTopics
PublishedTopics
```

Example:

```
test.PlusAgent
A,B
C
```

---

# HTTP Endpoints

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | /app/* | Load HTML pages |
| POST | /upload | Upload a configuration file |
| GET | /publish | Publish a message to a Topic |

---

# Design Principles

The project was designed according to common software engineering principles, including:

- Separation of Concerns
- Single Responsibility Principle (SRP)
- Interface-based design
- Modular architecture
- Thread-safe programming
- Reusable HTTP framework

---

# Concurrency

The project supports concurrent execution using:

- Thread Pool (ExecutorService)
- BlockingQueue
- Parallel Agents
- ConcurrentHashMap
- CopyOnWriteArrayList

---

# External Library

Graph visualization is implemented using **vis-network**:

https://visjs.github.io/vis-network/

---

# Notes

This project was developed as the final integration exercise of the Advanced Programming course.

Its goal was to combine the concepts learned throughout the semester—including concurrency, reflection, networking, HTTP communication, dynamic configuration loading, and graph visualization—into one complete and reusable software system.

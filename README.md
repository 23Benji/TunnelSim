# Tunnel Visitor Simulation

<p align="center">
  <img src="img/img.png" alt="Client GUI Screenshot">
</p>

## Project Description

This project simulates visitor flow management for a capacity-limited resource, modeled after an archaeological tunnel scenario. It features a client-server architecture where multiple entrance clients interact with a central server to manage visitor entry and exit based on available guides and overall tunnel capacity.

### Scenario Context

The simulation is based on the following scenario:

*   An archaeologically significant tunnel is to be made accessible to the public.
*   The tunnel has two entrances, through which visitors can only tour the tunnel in groups accompanied by a guide.
*   There are four guides available *at each entrance* (managed locally by each client in this simulation).
*   For safety reasons, a maximum of 50 visitors may be in the tunnel simultaneously (managed globally by the server).
*   A guide accompanies a group from their entrance into the tunnel and returns with the group to the same entrance before starting another tour.

This Java application simulates this system, with graphical clients representing the entrances and a server managing the central tunnel capacity.

## Features

*   **Client-Server Architecture:** Uses Java Sockets (TCP/IP) for communication between entrances (clients) and the central monitor (server).
*   **Multiple Entrances:** Supports running multiple client instances simultaneously, each representing a separate tunnel entrance.
*   **Graphical User Interface:** Swing-based GUI for each client entrance, featuring:
    *   Modern look and feel (Nimbus with custom earthy/muted color palette).
    *   Rounded buttons and styled components.
    *   Display of available local guides.
    *   Input for requesting visits (number of visitors).
    *   List display of currently active visits originating from that entrance.
    *   Button to finish a selected active visit.
    *   Display of current overall visitor capacity available in the tunnel (polled from server).
    *   Real-time status log area.
*   **Guide Management:** Each client manages its own pool of local guides (`GuidesMonitor`).
*   **Capacity Management:** The server (`TunnelServer`, `VisitorsMonitor`) manages the overall tunnel capacity (max 50 visitors).
*   **Concurrency:** Handles concurrent requests from multiple clients using Java Threads.
*   **Periodic Checks:** Clients periodically query the server for the current available visitor capacity.

## Technology Stack

*   **Language:** Java (Requires JDK 8 or later recommended)
*   **Networking:** Java Sockets (`java.net.Socket`, `java.net.ServerSocket`)
*   **GUI:** Java Swing (using Nimbus Look & Feel)
*   **Concurrency:** Java Threads, `synchronized`, `wait`/`notifyAll`

## Setup and Running

1.  **Prerequisites:**
    *   Java Development Kit (JDK) version 8 or higher installed and configured (check with `java -version` and `javac -version`).

2.  **Compilation:**
    *   Navigate to the project's root directory (the one containing the `tunnel` folder) in your terminal or command prompt.
    *   Compile the Java source files:
        ```bash
        # On Windows (adjust path separator if needed)
        javac tunnel\server\*.java tunnel\client\*.java

        # On Linux/macOS
        javac tunnel/server/*.java tunnel/client/*.java
        ```
    *   This will create `.class` files within their respective package directories.

3.  **Running the Application:**
    *   **Start the Server:** Run the server component first. It will listen for client connections.
        ```bash
        java tunnel.server.TunnelServer
        ```
        *(Note: Replace `TunnelServer` with the actual name of your server's main class if different)*
    *   **Start the Clients (Entrances):** Open **two separate** terminal/command prompt windows. In each window, run the client application. The `main` method is designed to launch two instances side-by-side if run once, but running it explicitly twice ensures separate processes if needed.
        ```bash
        # In the first client terminal
        java tunnel.client.ClientForm

        # In the second client terminal
        java tunnel.client.ClientForm
        ```
        *(The code should automatically position the second window next to the first one)*

    *   You should now have the server running (likely just showing console output) and two client GUI windows ("Entrance 1" and "Entrance 2") on your screen. You can interact with each client independently to request and finish visits.

## Potential Future Improvements

*   Implement simulated visit durations.
*   Move guide management fully to the server for a global pool.
*   Add a server-side admin interface.
*   Implement more detailed server responses and client feedback.
*   Add configuration files for server host/port, capacities, etc.

---

## License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

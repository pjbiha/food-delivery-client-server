# Food Delivery client-server
Simple client–server demo for browsing stores and ordering food.

This project contains a Java backend (Master / Worker / Reducer) and an Android frontend.  
The Android app connects via plain TCP sockets to a local backend.



> Note: Check the port constants in source if you changed anything. Default ports used in the code-base:
> - Dummy client/server port: `5005` (Android client connects to `10.0.2.2:5005`)
> - Master server listens on port `5000` (see `MasterServer.java`)
> - Reducer listens on a reducer port (see `Reducer.java`) — ensure Master and Reducer ports match in your local copy
> - Worker servers accept an IP and port argument when started

---


### Backend (run in separate terminal windows)
Navigate to the backend source folder (where `.java` files are compiled / runnable). Example run order:

```bash
# 1) Start reducer (expects number of workers as argument)
#    e.g. 2 workers:
java Reducer 2

# 2) Start master server and pass each worker's IP and port as pairs:
#    Example: two workers at 127.0.0.1:8000 and 127.0.0.1:8001
#    Usage: java MasterServer <worker_ip1> <worker_port1> <worker_ip2> <worker_port2> ...
java MasterServer 127.0.0.1 8000 127.0.0.1 8001

# 3) Start one or more worker servers (each in its own terminal).
#    Usage: java WorkerServer <ip> <port>
java WorkerServer 127.0.0.1 8000
java WorkerServer 127.0.0.1 8001

# 4) (Optional) Start the DummyApp (a simple console client that drives the backend)
java DummyApp
```

The worker ports you pass to `MasterServer` must match the ports where each `WorkerServer` is listening.

---

### Android frontend

1. Open the `app/` module in Android Studio.  
2. Make sure the emulator is running (or a device connected). The app expects the backend to be reachable at host `10.0.2.2` (emulator → host).  
3. Build & Run the `app` module.  
   * The app's `MainActivity` will connect to `10.0.2.2:5005` and interact with the backend via the DummyApp/Master/Workers chain.

---

## Example

Start backend components (in separate terminals) and then run the Android app:

```bash
# example sequence
java Reducer 2
java WorkerServer 127.0.0.1 8000
java WorkerServer 127.0.0.1 8001
java MasterServer 127.0.0.1 8000 127.0.0.1 8001
java DummyApp
# then run Android app in emulator (it connects to 10.0.2.2:5005)
```

---

## What it does

Implements a simple **Master–Worker** backend topology:

- **MasterServer** receives client requests and forwards jobs to workers.  
- **WorkerServer(s)** keep local stores, products, sales and rating logic.  
- **Reducer** aggregates worker replies and returns combined results to Master.  

The **Android app**:

- Connects to the backend via TCP sockets.  
- Requests and displays a list of stores (with basic metadata).  
- Lets the user filter by distance or custom filters (category / stars / price-range).  
- Shows product lists for a store and lets the user place orders and rate stores.   

The code uses plain Java sockets and simple text-based commands between components (no HTTP, no external libraries).

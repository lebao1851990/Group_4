package com.csd201.dungeon.controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.ds.Node;
import com.csd201.dungeon.ds.MyLinkedList;

public class GameServer {
    private GameController controller;

    public GameServer(GameController controller) {
        this.controller = controller;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/map", new MapHandler());
        server.createContext("/api/move", new MoveHandler());
        server.createContext("/api/attack", new AttackHandler());
        
        server.setExecutor(null); 
        server.start();
        System.out.println("🚀 API Server is running on http://localhost:" + port);
        System.out.println("CORS is enabled for all endpoints.");
    }

    private class AttackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [ATTACK] Received request");
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Room current = controller.getRooms()[controller.getCurrentRoomId()];
            Monster m = current.getMonster();
            boolean success = false;
            if (m != null && m.isAlive()) {
                m.takeDamage(controller.getPlayer().getAttack());
                success = true;
                if (m.isAlive()) {
                    controller.getPlayer().takeDamage(m.getAttack());
                }
            }
            String json = String.format("{\"success\": %b, \"monsterHp\": %d, \"playerHp\": %d}", 
                success, (m != null ? m.getHp() : 0), controller.getPlayer().getHp());
            sendResponse(exchange, json);
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [STATUS] Received request");
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String json = String.format("{\"currentRoomId\": %d, \"playerHp\": %d, \"playerAtk\": %d}",
                    controller.getCurrentRoomId(),
                    controller.getPlayer().getHp(),
                    controller.getPlayer().getAttack());
            
            sendResponse(exchange, json);
        }
    }

    private class MapHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [MAP] Received request");
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{\"rooms\": [");
            Room[] rooms = controller.getRooms();
            for (int i = 0; i < rooms.length; i++) {
                Monster m = rooms[i].getMonster();
                String mJson = (m == null) ? "null" : 
                    String.format("{\"id\": %d, \"name\": \"%s\", \"hp\": %d, \"atk\": %d}", 
                        m.getId(), m.getName(), m.getHp(), m.getAttack());
                
                sb.append(String.format("{\"id\": %d, \"name\": \"%s\", \"monster\": %s}", 
                        rooms[i].getId(), rooms[i].getName(), mJson));
                if (i < rooms.length - 1) sb.append(",");
            }
            sb.append("], \"edges\": {");
            for (int i = 0; i < rooms.length; i++) {
                sb.append("\"").append(i).append("\": [");
                MyLinkedList<Integer> neighbors = controller.getMap().getNeighbors(i);
                Node<Integer> cur = neighbors.getHead();
                while (cur != null) {
                    sb.append(cur.data);
                    if (cur.next != null) sb.append(",");
                    cur = cur.next;
                }
                sb.append("]");
                if (i < rooms.length - 1) sb.append(",");
            }
            sb.append("}}");
            
            sendResponse(exchange, sb.toString());
        }
    }

    private class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("DEBUG: [MOVE] Received request");
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            int targetId = -1;
            if (query != null && query.contains("id=")) {
                try {
                    targetId = Integer.parseInt(query.split("id=")[1]);
                } catch (Exception e) {}
            }

            // Logic check move
            boolean success = false;
            MyLinkedList<Integer> neighbors = controller.getMap().getNeighbors(controller.getCurrentRoomId());
            Node<Integer> cur = neighbors.getHead();
            while (cur != null) {
                if (cur.data == targetId) {
                    controller.setCurrentRoomId(targetId);
                    success = true;
                    break;
                }
                cur = cur.next;
            }

            String json = String.format("{\"success\": %b, \"newRoomId\": %d}", success, controller.getCurrentRoomId());
            sendResponse(exchange, json);
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
    }

    private void sendResponse(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

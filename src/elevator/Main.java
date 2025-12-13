package elevator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int minFloor = 1, maxFloor = 10;
        int elevatorsCount = 3;

        Dispatcher dispatcher = new Dispatcher(elevatorsCount, minFloor);

        System.out.println("Симуляция лифтового управления");
        System.out.println("Elevators count: " + elevatorsCount);
        System.out.println("Available commands:");
        System.out.println("<этаж> up        — вызвать лифт вверх");
        System.out.println("<этаж> down      — вызвать лифт вниз");
        System.out.println("status           — показать состояние лифтов");
        System.out.println("save <файл>      — сохранить состояние");
        System.out.println("load <файл>      — загрузить состояние");
        System.out.println("exit             — завершить программу");

        while (true) {
            String line = sc.nextLine().trim();
            if (line.equals("exit")) {
                dispatcher.stopAll();
                break;
            } else if (line.equals("status")) {
                dispatcher.status();
            } else if (line.startsWith("save ")) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    dispatcher.save(parts[1]);
                } else {
                    System.out.println("Ошибка: некорректный формат команды save.");
                }
            } else if (line.startsWith("load ")) {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    Dispatcher loaded = Dispatcher.load(parts[1]);
                    if (loaded != null) {
                        dispatcher = loaded;
                    }
                } else {
                    System.out.println("Ошибка: некорректный формат команды load.");
                }
            } else {
                String[] parts = line.split("\\s+");
                if (parts.length == 2) {
                    try {
                        int floor = Integer.parseInt(parts[0]);
                        Direction dir;
                        if (parts[1].equalsIgnoreCase("up")) {
                            dir = Direction.UP;
                        } else if (parts[1].equalsIgnoreCase("down")) {
                            dir = Direction.DOWN;
                        } else {
                            System.out.println("Ошибка: неизвестное направление '" + parts[1] + "'. Используйте 'up' или 'down'.");
                            continue;
                        }
                        dispatcher.requestElevator(floor, dir);
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка: некорректный этаж '" + parts[0] + "'. Введите число.");
                    }
                } else {
                    System.out.println("Ошибка: неизвестная команда. Проверьте формат ввода.");
                }
            }
        }
        System.out.println("Программа завершена.");
    }
}

limit = 100.5
greeting = "Start"
items = [1, 2, 3.0, 'hello']
for i in items:
    if (i % 2 == 0) and not (i < 0):
        print(greeting)
        result = i ** 2 // 3
    else:
        while result >= 10.0 or i != 5:
            result = result - 1

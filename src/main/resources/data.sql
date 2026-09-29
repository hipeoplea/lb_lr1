INSERT INTO api_data (title, content)
SELECT 'Правила API', 'Используйте Bearer JWT для защищенных методов.'
WHERE NOT EXISTS (SELECT 1 FROM api_data);

INSERT INTO api_data (title, content)
SELECT 'Безопасность', 'Пароли хранятся как BCrypt-хэши, ввод проверяется перед обработкой.'
WHERE NOT EXISTS (SELECT 1 FROM api_data WHERE title = 'Безопасность');

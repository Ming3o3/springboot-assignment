-- 检查角色表数据
SELECT * FROM roles;

-- 检查用户表数据
SELECT id, username, email, status FROM users;

-- 检查用户角色关联
SELECT ur.id, u.username, r.role_name, r.role_code 
FROM user_roles ur
INNER JOIN users u ON ur.user_id = u.id
INNER JOIN roles r ON ur.role_id = r.id;

-- 检查admin用户的角色
SELECT u.username, r.role_code, r.role_name
FROM users u
INNER JOIN user_roles ur ON u.id = ur.user_id
INNER JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin';

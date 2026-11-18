/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion_pr2;

/**
 *
 * @author lenovo
 */
import javax.swing.JOptionPane;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class SQL_Conexion {

    Conexion_Pr2 cone = new Conexion_Pr2();
    Connection conexion = cone.getConnection();

    // Modelo de tabla no editable (auxiliar)
    private DefaultTableModel crearModeloNoEditable(String[] columnas) {
        return new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // ==================== INSERTS (vía Procedimientos Almacenados) ====================

    public boolean CALLCliente(String name, String mail, String address) {
        String sql_accion = "{CALL pa_insertCliente (?, ?, ?)}";
        try (CallableStatement cs = conexion.prepareCall(sql_accion)) {
            cs.setString(1, name);
            cs.setString(2, mail);
            cs.setString(3, address);
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al llamar el PA: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean CALLPedido(double Total, int IDCliente) {
        String sql_accion = "{CALL pa_insertPedido (?, ?)}";
        try (CallableStatement cs = conexion.prepareCall(sql_accion)) {
            cs.setDouble(1, Total);
            cs.setInt(2, IDCliente);
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al llamar el PA: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean CALLProductos(String name_pr, double precio, int stock) {
        String sql = "{CALL pa_insertProductos (?, ?, ?)}";
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setString(1, name_pr);
            cs.setDouble(2, precio);
            cs.setInt(3, stock);
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al llamar al PA: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean CALLDet_Pedido(int cantidad, double precio, int idPedido, int idProductos) {
        String sql = "{CALL pa_insertDet_Pedido (?, ?, ?, ?)}";
        try (CallableStatement cs = conexion.prepareCall(sql)) {
            cs.setInt(1, cantidad);
            cs.setDouble(2, precio);
            cs.setInt(3, idPedido);
            cs.setInt(4, idProductos);
            cs.execute();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al llamar al PA: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== SELECTS ====================

    public DefaultTableModel mostrarClientes(String idBusqueda) {
        String[] columnas = {"ID Cliente", "Nombre", "Correo", "Dirección"};
        DefaultTableModel modelo = crearModeloNoEditable(columnas);
        String sql = (idBusqueda == null || idBusqueda.trim().isEmpty())
                ? "SELECT * FROM Cliente"
                : "SELECT * FROM Cliente WHERE ID_Cliente = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            if (idBusqueda != null && !idBusqueda.trim().isEmpty()) {
                ps.setInt(1, Integer.parseInt(idBusqueda.trim()));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("ID_Cliente"),
                    rs.getString("Name_Cl"),
                    rs.getString("Mail_Cl"),
                    rs.getString("Dire_Cl")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar clientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    public DefaultTableModel mostrarPedidos(String idBusqueda) {
        String[] columnas = {"ID Pedido", "Fecha", "Total", "ID Cliente"};
        DefaultTableModel modelo = crearModeloNoEditable(columnas);
        String sql = (idBusqueda == null || idBusqueda.trim().isEmpty())
                ? "SELECT * FROM Pedido"
                : "SELECT * FROM Pedido WHERE ID_Pedido = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            if (idBusqueda != null && !idBusqueda.trim().isEmpty()) {
                ps.setInt(1, Integer.parseInt(idBusqueda.trim()));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("ID_Pedido"),
                    rs.getTimestamp("FPedido"),
                    rs.getDouble("Total_Ped"),
                    rs.getInt("ID_Cliente")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar pedidos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    public DefaultTableModel mostrarProductos(String idBusqueda) {
        String[] columnas = {"ID Producto", "Nombre", "Precio", "Stock"};
        DefaultTableModel modelo = crearModeloNoEditable(columnas);
        String sql = (idBusqueda == null || idBusqueda.trim().isEmpty())
                ? "SELECT * FROM Productos"
                : "SELECT * FROM Productos WHERE ID_Productos = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            if (idBusqueda != null && !idBusqueda.trim().isEmpty()) {
                ps.setInt(1, Integer.parseInt(idBusqueda.trim()));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("ID_Productos"),
                    rs.getString("Name_Pro"),
                    rs.getDouble("Precio_Pro"),
                    rs.getInt("Stock")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar productos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    // Solo productos con stock disponible (para el módulo de venta / pedido)
    public DefaultTableModel mostrarProductosParaVenta() {
        String[] columnas = {"ID Producto", "Producto", "Precio", "Stock"};
        DefaultTableModel modelo = crearModeloNoEditable(columnas);
        String sql = "SELECT ID_Productos, Name_Pro, Precio_Pro, Stock FROM Productos WHERE Stock > 0";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("ID_Productos"),
                    rs.getString("Name_Pro"),
                    rs.getDouble("Precio_Pro"),
                    rs.getInt("Stock")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar catálogo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    public DefaultTableModel mostrarDetPedido(String idBusqueda) {
        String[] columnas = {"ID Detalle", "Cantidad", "Precio Unitario", "ID Pedido", "ID Producto"};
        DefaultTableModel modelo = crearModeloNoEditable(columnas);
        String sql = (idBusqueda == null || idBusqueda.trim().isEmpty())
                ? "SELECT * FROM Det_Pedido"
                : "SELECT * FROM Det_Pedido WHERE ID_Detalle = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            if (idBusqueda != null && !idBusqueda.trim().isEmpty()) {
                ps.setInt(1, Integer.parseInt(idBusqueda.trim()));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("ID_Detalle"),
                    rs.getInt("Cantidad"),
                    rs.getDouble("Precio_Uni"),
                    rs.getInt("ID_Pedido"),
                    rs.getInt("ID_Productos")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Error al consultar detalle de pedido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    // ==================== UPDATES ====================

    public boolean updateCliente(int idCliente, String name, String mail, String dire) {
        String sql = "UPDATE Cliente SET Name_Cl = ?, Mail_Cl = ?, Dire_Cl = ? WHERE ID_Cliente = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, mail);
            ps.setString(3, dire);
            ps.setInt(4, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar cliente: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updatePedido(int idPedido, double totalPed, int idCliente) {
        String sql = "UPDATE Pedido SET Total_Ped = ?, ID_Cliente = ? WHERE ID_Pedido = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setDouble(1, totalPed);
            ps.setInt(2, idCliente);
            ps.setInt(3, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar pedido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateProducto(int idProducto, String name, double precio, int stock) {
        String sql = "UPDATE Productos SET Name_Pro = ?, Precio_Pro = ?, Stock = ? WHERE ID_Productos = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, name);
            ps.setDouble(2, precio);
            ps.setInt(3, stock);
            ps.setInt(4, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateDetPedido(int idDetalle, int cantidad, double precioUni, int idPedido, int idProducto) {
        String sql = "UPDATE Det_Pedido SET Cantidad = ?, Precio_Uni = ?, ID_Pedido = ?, ID_Productos = ? WHERE ID_Detalle = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, cantidad);
            ps.setDouble(2, precioUni);
            ps.setInt(3, idPedido);
            ps.setInt(4, idProducto);
            ps.setInt(5, idDetalle);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar detalle de pedido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== DELETES ====================

    public boolean deleteDetalle(int idDetalle) {
        String sql = "DELETE FROM Det_Pedido WHERE ID_Detalle = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idDetalle);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar detalle: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deletePedido(int idPedido) {
        String sql = "DELETE FROM Pedido WHERE ID_Pedido = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idPedido);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar pedido (verifica que no tenga detalles): " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteProducto(int idProducto) {
        String sql = "DELETE FROM Productos WHERE ID_Productos = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar producto (verifica que no esté en un detalle): " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteCliente(int idCliente) {
        String sql = "DELETE FROM Cliente WHERE ID_Cliente = ?";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar cliente (verifica que no tenga pedidos): " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== COMBOS ====================

    public javax.swing.DefaultComboBoxModel<String> obtenerComboClientes() {
        javax.swing.DefaultComboBoxModel<String> modelo = new javax.swing.DefaultComboBoxModel<>();
        String sql = "SELECT ID_Cliente, Name_Cl FROM Cliente";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelo.addElement(rs.getInt("ID_Cliente") + " - " + rs.getString("Name_Cl"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar clientes en combo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    public javax.swing.DefaultComboBoxModel<String> obtenerComboProductos() {
        javax.swing.DefaultComboBoxModel<String> modelo = new javax.swing.DefaultComboBoxModel<>();
        String sql = "SELECT ID_Productos, Name_Pro FROM Productos";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelo.addElement(rs.getInt("ID_Productos") + " - " + rs.getString("Name_Pro"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar productos en combo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }

    public javax.swing.DefaultComboBoxModel<String> obtenerComboPedidos() {
        javax.swing.DefaultComboBoxModel<String> modelo = new javax.swing.DefaultComboBoxModel<>();
        String sql = "SELECT ID_Pedido, Total_Ped FROM Pedido";
        try {
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modelo.addElement(rs.getInt("ID_Pedido") + " - Total: " + rs.getDouble("Total_Ped"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar pedidos en combo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return modelo;
    }
}
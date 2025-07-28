package com.psoft.wallet.service;

import org.springframework.stereotype.Service;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.repository.ClienteRepository;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente criarCliente(Cliente cliente) {
        // Validar código de acesso (6 dígitos)
        if (cliente.getCodigoAcesso() == null || cliente.getCodigoAcesso().length() != 6 || 
            !cliente.getCodigoAcesso().matches("\\d{6}")) {
            throw new IllegalArgumentException("Código de acesso deve ter exatamente 6 dígitos");
        }
        
        return repository.save(cliente);
    }

    public List<Cliente> listarTodosClientes() {
        List<Cliente> clientes = repository.findAll();
        // Não exibir códigos de acesso nas operações de leitura
        clientes.forEach(cliente -> cliente.setCodigoAcesso(null));
        return clientes;
    }

    public Cliente buscarClientePorId(Long id) {
        Cliente cliente = repository.findById(id)
            .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente com ID " + id + " não encontrado"));
        
        // Não exibir código de acesso
        cliente.setCodigoAcesso(null);
        return cliente;
    }

    public Cliente editarCliente(Long id, Cliente cliente, String codigoAcesso) {
        Cliente clienteExistente = repository.findById(id)
            .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente com ID " + id + " não encontrado"));

        // Verificar código de acesso
        if (codigoAcesso == null || !codigoAcesso.equals(clienteExistente.getCodigoAcesso())) {
            throw new CodigoAcessoIncorretoException("Código de acesso incorreto ou não informado");
        }

        // Validar novo código de acesso se fornecido
        if (cliente.getCodigoAcesso() != null) {
            if (cliente.getCodigoAcesso().length() != 6 || !cliente.getCodigoAcesso().matches("\\d{6}")) {
                throw new IllegalArgumentException("Código de acesso deve ter exatamente 6 dígitos");
            }
        }

        // Atualizar campos
        clienteExistente.setNomeCompleto(cliente.getNomeCompleto());
        clienteExistente.setEnderecoPrincipal(cliente.getEnderecoPrincipal());
        clienteExistente.setPlano(cliente.getPlano());
        if (cliente.getCodigoAcesso() != null) {
            clienteExistente.setCodigoAcesso(cliente.getCodigoAcesso());
        }

        Cliente clienteSalvo = repository.save(clienteExistente);
        // Não retornar código de acesso
        clienteSalvo.setCodigoAcesso(null);
        return clienteSalvo;
    }

    public void removerCliente(Long id, String codigoAcesso) {
        Cliente cliente = repository.findById(id)
            .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente com ID " + id + " não encontrado"));

        // Verificar código de acesso
        if (codigoAcesso == null || !codigoAcesso.equals(cliente.getCodigoAcesso())) {
            throw new CodigoAcessoIncorretoException("Código de acesso incorreto ou não informado");
        }

        repository.deleteById(id);
    }

    public List<Cliente> listarAtivosPorPlano(String codigoAcesso) {
        if (codigoAcesso == null) {
            throw new CodigoAcessoIncorretoException("Código de acesso é obrigatório");
        }

        Cliente cliente = repository.findByCodigoAcesso(codigoAcesso)
            .orElseThrow(() -> new CodigoAcessoIncorretoException("Código de acesso incorreto"));

        // Retornar apenas o cliente (sem código de acesso) para validação
        cliente.setCodigoAcesso(null);
        return List.of(cliente);
    }
} 
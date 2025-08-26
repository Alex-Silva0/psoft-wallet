package com.psoft.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psoft.wallet.dto.ClienteRequestDTO;
import com.psoft.wallet.dto.ClienteResponseDTO;
import com.psoft.wallet.model.Cliente;
import com.psoft.wallet.enums.TipoPlano;
import com.psoft.wallet.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClienteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private String clienteAsJsonString(Cliente cliente) throws Exception {
        // Cria um Map para contornar a anotação @JsonProperty(WRITE_ONLY) do modelo
        // e garantir que o código de acesso seja incluído no payload do teste.
        Map<String, Object> clienteMap = new java.util.HashMap<>();
        clienteMap.put("nomeCompleto", cliente.getNomeCompleto());
        clienteMap.put("enderecoPrincipal", cliente.getEnderecoPrincipal());
        clienteMap.put("plano", cliente.getPlano());
        clienteMap.put("codigoAcesso", cliente.getCodigoAcesso());
        return objectMapper.writeValueAsString(clienteMap);
    }

    // US04 - Testes para criar, ler, editar e remover clientes

    @Test
    void testCriarClienteComSucesso() throws Exception {
        // Given
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("123456");

        // When & Then
        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$.enderecoPrincipal").value("Rua das Flores, 123"))
                .andExpect(jsonPath("$.plano").value("NORMAL"))
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        // Verificar se foi salvo no banco
        ClienteResponseDTO clienteSalvo = objectMapper.readValue(response, ClienteResponseDTO.class);
        assertTrue(repository.findById(clienteSalvo.getId()).isPresent());
        
        // Verificar se o código de acesso foi salvo (mas não retornado)
        Cliente clienteNoBanco = repository.findById(clienteSalvo.getId()).orElse(null);
        assertNotNull(clienteNoBanco);
        assertEquals("123456", clienteNoBanco.getCodigoAcesso());
    }

    @Test
    void testCriarClientePremium() throws Exception {
        // Given
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("Maria Santos");
        clienteRequest.setEnderecoPrincipal("Av. Principal, 456");
        clienteRequest.setPlano(TipoPlano.PREMIUM);
        clienteRequest.setCodigoAcesso("654321");

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plano").value("PREMIUM"))
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist());
    }

    @Test
    void testCriarClienteComCodigoAcessoInvalido() throws Exception {
        // Given - Código com menos de 6 dígitos
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("12345");

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Código de acesso deve ter exatamente 6 dígitos"));
    }

    @Test
    void testCriarClienteComCodigoAcessoComLetras() throws Exception {
        // Given - Código com letras
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("12345a");

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Código de acesso deve ter exatamente 6 dígitos"));
    }

    @Test
    void testCriarClienteSemCodigoAcesso() throws Exception {
        // Given - Sem código de acesso
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("");

        // When & Then
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Código de acesso deve ter exatamente 6 dígitos"));
    }

    @Test
    void testListarTodosClientes() throws Exception {
        // Given - Criar múltiplos clientes
        ClienteRequestDTO cliente1 = new ClienteRequestDTO();
        cliente1.setNomeCompleto("João Silva");
        cliente1.setEnderecoPrincipal("Rua das Flores, 123");
        cliente1.setPlano(TipoPlano.NORMAL);
        cliente1.setCodigoAcesso("123456");

        ClienteRequestDTO cliente2 = new ClienteRequestDTO();
        cliente2.setNomeCompleto("Maria Santos");
        cliente2.setEnderecoPrincipal("Av. Principal, 456");
        cliente2.setPlano(TipoPlano.PREMIUM);
        cliente2.setCodigoAcesso("654321");

        // Criar os clientes
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cliente2)))
                .andExpect(status().isOk());

        // When & Then - Listar todos os clientes
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$[1].nomeCompleto").value("Maria Santos"))
                .andExpect(jsonPath("$[0].codigoAcesso").doesNotExist())
                .andExpect(jsonPath("$[1].codigoAcesso").doesNotExist());
    }

    @Test
    void testBuscarClientePorId() throws Exception {
        // Given - Criar um cliente
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ClienteResponseDTO clienteSalvo = objectMapper.readValue(response, ClienteResponseDTO.class);
        Long id = clienteSalvo.getId();

        // When & Then - Buscar cliente por ID
        mockMvc.perform(get("/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva"))
                .andExpect(jsonPath("$.enderecoPrincipal").value("Rua das Flores, 123"))
                .andExpect(jsonPath("$.plano").value("NORMAL"))
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist());
    }

    @Test
    void testBuscarClientePorIdNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(get("/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente com ID 999 não encontrado"));
    }

    @Test
    void testEditarClienteComCodigoAcessoCorreto() throws Exception {
        // Given - Criar um cliente
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ClienteResponseDTO clienteSalvo = objectMapper.readValue(response, ClienteResponseDTO.class);
        Long id = clienteSalvo.getId();

        // When & Then - Editar cliente com código correto
        ClienteRequestDTO clienteEditado = new ClienteRequestDTO();
        clienteEditado.setNomeCompleto("João Silva Santos");
        clienteEditado.setEnderecoPrincipal("Rua das Flores, 456");
        clienteEditado.setPlano(TipoPlano.PREMIUM);
        clienteEditado.setCodigoAcesso("123456");

        mockMvc.perform(put("/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteEditado))
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva Santos"))
                .andExpect(jsonPath("$.enderecoPrincipal").value("Rua das Flores, 456"))
                .andExpect(jsonPath("$.plano").value("PREMIUM"))
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist());
    }

    @Test
    void testEditarClienteComCodigoAcessoIncorreto() throws Exception {
        // Given - Criar um cliente
        ClienteRequestDTO clienteRequest = new ClienteRequestDTO();
        clienteRequest.setNomeCompleto("João Silva");
        clienteRequest.setEnderecoPrincipal("Rua das Flores, 123");
        clienteRequest.setPlano(TipoPlano.NORMAL);
        clienteRequest.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ClienteResponseDTO clienteSalvo = objectMapper.readValue(response, ClienteResponseDTO.class);
        Long id = clienteSalvo.getId();

        // When & Then - Tentar editar com código incorreto
        ClienteRequestDTO clienteEditado = new ClienteRequestDTO();
        clienteEditado.setNomeCompleto("João Silva Santos");
        clienteEditado.setEnderecoPrincipal("Rua das Flores, 123");
        clienteEditado.setPlano(TipoPlano.NORMAL);
        clienteEditado.setCodigoAcesso("123456");

        mockMvc.perform(put("/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteEditado))
                .param("codigoAcesso", "999999"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Código de acesso incorreto ou não informado"));
    }

    @Test
    void testEditarClienteSemCodigoAcesso() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // When & Then - Tentar editar sem código de acesso
        Cliente clienteEditado = new Cliente();
        clienteEditado.setNomeCompleto("João Silva Santos");

        mockMvc.perform(put("/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteEditado)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEditarClienteComNovoCodigoAcesso() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // When & Then - Editar cliente incluindo novo código de acesso
        Cliente clienteEditado = new Cliente();
        clienteEditado.setNomeCompleto("João Silva Santos");
        clienteEditado.setEnderecoPrincipal("Rua das Flores, 456");
        clienteEditado.setPlano(TipoPlano.PREMIUM);
        clienteEditado.setCodigoAcesso("654321");

        mockMvc.perform(put("/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(clienteEditado))
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva Santos"))
                .andExpect(jsonPath("$.plano").value("PREMIUM"))
                .andExpect(jsonPath("$.codigoAcesso").doesNotExist());

        // Verificar se o novo código foi salvo
        Cliente clienteNoBanco = repository.findById(id).orElse(null);
        assertNotNull(clienteNoBanco);
        assertEquals("654321", clienteNoBanco.getCodigoAcesso());
    }

    @Test
    void testEditarClienteComNovoCodigoAcessoInvalido() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // When & Then - Tentar editar com novo código inválido
        Cliente clienteEditado = new Cliente();
        clienteEditado.setNomeCompleto("João Silva Santos");
        clienteEditado.setCodigoAcesso("12345"); // Menos de 6 dígitos

        mockMvc.perform(put("/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(clienteEditado))
                .param("codigoAcesso", "123456"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Código de acesso deve ter exatamente 6 dígitos"));
    }

    @Test
    void testRemoverClienteComCodigoAcessoCorreto() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // When & Then - Remover cliente com código correto
        mockMvc.perform(delete("/clientes/{id}", id)
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk());

        // Verificar se foi removido do banco
        assertFalse(repository.existsById(id));
    }

    @Test
    void testRemoverClienteComCodigoAcessoIncorreto() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // When & Then - Tentar remover com código incorreto
        mockMvc.perform(delete("/clientes/{id}", id)
                .param("codigoAcesso", "999999"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Código de acesso incorreto ou não informado"));

        // Verificar se não foi removido
        assertTrue(repository.existsById(id));
    }

    @Test
    void testRemoverClienteSemCodigoAcesso() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // When & Then - Tentar remover sem código de acesso
        mockMvc.perform(delete("/clientes/{id}", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoverClienteNaoEncontrado() throws Exception {
        // When & Then
        mockMvc.perform(delete("/clientes/999")
                .param("codigoAcesso", "123456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente com ID 999 não encontrado"));
    }

    @Test
    void testFluxoCompletoCriarEditarRemover() throws Exception {
        // Given - Criar um cliente
        Cliente cliente = new Cliente();
        cliente.setNomeCompleto("João Silva");
        cliente.setEnderecoPrincipal("Rua das Flores, 123");
        cliente.setPlano(TipoPlano.NORMAL);
        cliente.setCodigoAcesso("123456");

        // When & Then - 1. Criar cliente
        String response = mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clienteAsJsonString(cliente)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Cliente clienteSalvo = objectMapper.readValue(response, Cliente.class);
        Long id = clienteSalvo.getId();

        // Verificar se foi criado
        assertTrue(repository.existsById(id));

        // When & Then - 2. Editar cliente
        ClienteRequestDTO clienteEditado = new ClienteRequestDTO();
        clienteEditado.setNomeCompleto("João Silva Santos");
        clienteEditado.setEnderecoPrincipal("Rua das Flores, 456");
        clienteEditado.setPlano(TipoPlano.PREMIUM);
        clienteEditado.setCodigoAcesso("123456");

        mockMvc.perform(put("/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteEditado))
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("João Silva Santos"))
                .andExpect(jsonPath("$.plano").value("PREMIUM"));

        // Verificar se foi editado
        Cliente clienteAtualizado = repository.findById(id).orElse(null);
        assertNotNull(clienteAtualizado);
        assertEquals("João Silva Santos", clienteAtualizado.getNomeCompleto());
        assertEquals(TipoPlano.PREMIUM, clienteAtualizado.getPlano());

        // When & Then - 3. Remover cliente
        mockMvc.perform(delete("/clientes/{id}", id)
                .param("codigoAcesso", "123456"))
                .andExpect(status().isOk());

        // Verificar se foi removido
        assertFalse(repository.existsById(id));
    }
} 
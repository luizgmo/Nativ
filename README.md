# Nativ - Micro Rede Social

Este projeto foi desenvolvido como parte do **Projeto Prático Avaliativo do 1º Bimestre** da disciplina de **Dispositivos Móveis 2 (ARQDMO2)**, no curso de Análise e Desenvolvimento de Sistemas do **IFSP - Campus Araraquara**.

O objetivo do aplicativo é permitir que utilizadores compartilhem fotos, textos e a sua localização atual (cidade) numa rede social sincronizada em tempo real.

## 📺 Demonstração

> - Demonstração das funcionalidades básicas.

---

## 🚀 Funcionalidades Implementadas

### 1. Autenticação (Firebase Authentication)
- **[RF1-1/2/3]:** Telas de Login e Cadastro com validação de campos (e-mail, senha, nome completo).
- **[RF1-4]:** Persistência de login: o app reconhece o utilizador autenticado e redireciona automaticamente para a Home.

### 2. Postagens e Localização (GPS & Firestore)
- **[RF2-1/2]:** Criação de posts com imagem da galeria (convertida para Base64), descrição e cidade.
- **[RF4-1]:** Integração com `FusedLocationProviderClient` e `Geocoder` para obter automaticamente o nome da cidade através das coordenadas de GPS.

### 3. Feed e Busca
- **[RF3-1]:** Feed com carregamento sob demanda (**Paginação de 5 em 5 itens** usando Timestamps do Firestore).
- **[RF3-2]:** Busca de postagens por cidade com filtro em tempo real (TextWatcher) que atualiza a lista conforme a digitação.

### 4. Perfil do Utilizador
- **[RF3-3]:** Tela de perfil que permite a edição dos dados cadastrais como nome e foto de perfil.

---

## 🛠️ Requisitos Não Funcionais (Técnicos)

- **Linguagem:** Kotlin.
- **API Mínima:** 33 (Android 13 Tiramisu).
- **Backend:** Firebase (Authentication e Firestore).
- **Arquitetura:** Uso de DAOs para separação de lógica de dados, View Binding para manipulação da UI e Helpers para localização e conversão de imagens.

---

## 📂 Estrutura do Código

- **`ui/`**: Contém as Activities (HomeActivity, ProfileActivity, Login, SignUp).
- **`adapter/`**: `PostAdapter` que gere a exibição e atualização dos itens no `RecyclerView`.
- **`utils/`**: 
    - `LocalizacaoHelper`: Lógica de permissões e Geocodificação.
    - `Base64Converter`: Utilitário para tratamento de imagens.
- **`model/` & `dao/`**: Definição de dados e acesso ao banco de dados Firestore.

---

## 🎥 Vídeo Explicativo do Código
> - Explicação detalhada da estrutura e lógica de programação.

---
**Desenvolvido por:** Luiz Gustavo Monico  
**Instituição:** IFSP Araraquara  
**Professor:** Henrique Galati

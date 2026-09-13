package projetoprincipiosdesign.entity;

public class Endereco {
    private String logradouro;
    private Cidade cidade;

    public Endereco(String logradouro, Cidade cidade) {
        this.logradouro = logradouro;
        this.cidade = cidade;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public String getEnderecoCompleto() {
        if (cidade != null && cidade.getNome() != null) {
            return logradouro + ", " + cidade.getNome();
        }
        return logradouro;
    }
}

package de.zettsystems.netzfilm.customer.adapter;

import de.zettsystems.netzfilm.customer.domain.Customer;
import de.zettsystems.netzfilm.customer.domain.CustomerRepository;
import de.zettsystems.netzfilm.customer.values.CustomerData;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ui/customer")
class CustomerController {
    private final CustomerRepository repository;

    CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @GetMapping(value = "/{id}")
    @Transactional(readOnly = true)
    public Customer findById(@PathVariable("id") Long id) {
        return repository.findById(id).orElseThrow();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Long create(@Valid @RequestBody CustomerData resource) {
        return repository.save(new Customer(resource.getName(), resource.getLastName(), resource.getBirthdate())).getId();
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void update(@PathVariable("id") Long id, @Valid @RequestBody CustomerData resource) {
        final Customer customer = repository.findByIdAndVersion(id, resource.getVersion()).orElseThrow();
        customer.updateData(resource.getName(), resource.getLastName(), resource.getBirthdate());
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable("id") Long id) {
        repository.deleteById(id);
    }

}
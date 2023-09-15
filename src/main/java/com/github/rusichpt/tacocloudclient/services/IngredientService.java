package com.github.rusichpt.tacocloudclient.services;

import com.github.rusichpt.tacocloudclient.Ingredient;

public interface IngredientService {
    public Iterable<Ingredient> findAll();

    public Ingredient addIngredient(Ingredient ingredient);
}

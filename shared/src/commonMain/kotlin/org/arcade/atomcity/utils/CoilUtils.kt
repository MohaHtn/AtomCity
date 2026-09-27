/**
 * Coil Image Loading Utilities
 *
 * Provides multiplatform extensions and helpers for Coil image loading and component registries.
 */
package org.arcade.atomcity.utils

import coil3.ComponentRegistry

expect fun ComponentRegistry.Builder.addAnimatedDecoders(): ComponentRegistry.Builder

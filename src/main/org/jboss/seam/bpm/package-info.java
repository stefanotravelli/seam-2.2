/**
 * A set of Seam components for business process management
 * via jBPM, including control of process and task instances,
 * rendering of task lists, and integration with jBPM.
 * 
 * The application may call components in this package
 * directly, or via EL, or may use them indirectly via
 * the annotations in org.jboss.seam.annotations.
 * 
 * @see org.jboss.seam.annotations.bpm
 */
@Namespace(value="http://jboss.com/products/seam/bpm", prefix="org.jboss.seam.bpm")
@AutoCreate
package org.jboss.seam.bpm;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Namespace;
